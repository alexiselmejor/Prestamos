package edu.universidad.presta.config;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Punto único de acceso a la base de datos, con un pool casero de conexiones.
 * Abrir una conexión nueva es lento (TCP + TLS + login), así que se reutilizan.
 * Quien llama a getConnection() sigue haciendo close() (try-with-resources); ese close()
 * devuelve la conexión al pool en vez de cerrarla.
 *
 * Diferencias con la versión original:
 *  - El pool es perezoso: no abre nada al cargar la clase, así que la base puede crearse antes
 *    (Database.init) y un SQL Server apagado no impide que arranque la aplicación.
 *  - Al devolver una conexión se restablece autoCommit=true (Db.tx lo apaga) y se hace rollback
 *    de lo que haya quedado pendiente; si no, los guardados siguientes no se confirmarían.
 *  - Se descartan las conexiones caídas y se reemplazan por otras nuevas.
 *  - Las credenciales vienen de AppConfig (app.properties / variables de entorno), no del código.
 */
public final class Conexion {

    private static final Logger LOG = Logger.getLogger(Conexion.class.getName());

    private static final int TAMANO_POOL = AppConfig.dbPoolSize();
    private static final BlockingQueue<Connection> libres = new ArrayBlockingQueue<>(TAMANO_POOL);
    private static final AtomicInteger creadas = new AtomicInteger();
    private static volatile boolean cerrado;

    private Conexion() {
        // Clase de utilidad: no se instancia
    }

    /** Conexión nueva sin pasar por el pool (la usa Database.init para hablar con "master"). */
    public static Connection abrirDirecta(String baseDeDatos) throws SQLException {
        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setServerName(AppConfig.dbHost());
        ds.setPortNumber(AppConfig.dbPort());
        ds.setDatabaseName(baseDeDatos);
        ds.setUser(AppConfig.dbUser());
        ds.setPassword(AppConfig.dbPassword());
        ds.setEncrypt("true");
        ds.setTrustServerCertificate(AppConfig.dbTrustCertificate());
        return ds.getConnection();
    }

    /** Toma una conexión del pool. Al cerrarla (try-with-resources) vuelve al pool, no se destruye. */
    public static Connection getConnection() throws SQLException {
        if (cerrado) {
            throw new SQLException("El pool de conexiones está cerrado.");
        }
        try {
            while (true) {
                Connection real = libres.poll();
                if (real == null) {
                    if (reservarCupo()) {
                        try {
                            return envolver(abrirDirecta(AppConfig.dbName()));
                        } catch (SQLException | RuntimeException e) {
                            creadas.decrementAndGet();
                            throw e;
                        }
                    }
                    // Pool lleno: esperar a que otro hilo devuelva una
                    real = libres.poll(15, TimeUnit.SECONDS);
                    if (real == null) {
                        throw new SQLException("Se agotó el tiempo esperando una conexión libre del pool.");
                    }
                }
                if (esUtil(real)) {
                    return envolver(real);
                }
                descartar(real);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrumpido esperando una conexión libre del pool", e);
        }
    }

    /** Cierra todas las conexiones libres (se llama al detener la aplicación). */
    public static void cerrar() {
        cerrado = true;
        Connection c;
        while ((c = libres.poll()) != null) {
            descartar(c);
        }
    }

    private static boolean reservarCupo() {
        while (true) {
            int n = creadas.get();
            if (n >= TAMANO_POOL) {
                return false;
            }
            if (creadas.compareAndSet(n, n + 1)) {
                return true;
            }
        }
    }

    private static boolean esUtil(Connection c) {
        try {
            return !c.isClosed() && c.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    private static void descartar(Connection real) {
        creadas.decrementAndGet();
        try {
            real.close();
        } catch (SQLException ignorada) {
            // ya estaba rota
        }
    }

    /** Deja la conexión limpia y la devuelve al pool; si no se puede, la descarta. */
    private static void devolver(Connection real) {
        try {
            if (cerrado || real.isClosed()) {
                descartar(real);
                return;
            }
            if (!real.getAutoCommit()) {
                real.rollback();
                real.setAutoCommit(true);
            }
            if (!libres.offer(real)) {
                descartar(real);
            }
        } catch (SQLException e) {
            LOG.log(Level.FINE, "Conexión descartada al devolverla al pool", e);
            descartar(real);
        }
    }

    /** Envuelve la conexión real: intercepta close() para devolverla al pool en vez de cerrarla. */
    private static Connection envolver(Connection real) {
        AtomicBoolean devuelta = new AtomicBoolean();
        InvocationHandler manejador = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method metodo, Object[] argumentos) throws Throwable {
                if ("close".equals(metodo.getName())) {
                    if (devuelta.compareAndSet(false, true)) {
                        devolver(real);
                    }
                    return null;
                }
                if (devuelta.get() && !"isClosed".equals(metodo.getName())) {
                    throw new SQLException("La conexión ya fue devuelta al pool.");
                }
                if ("isClosed".equals(metodo.getName()) && devuelta.get()) {
                    return Boolean.TRUE;
                }
                try {
                    return metodo.invoke(real, argumentos);
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            }
        };
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(), new Class<?>[] { Connection.class }, manejador);
    }
}
