package edu.universidad.presta.dao;

import edu.universidad.presta.util.Fechas;

/** Intentos de login fallidos (bloqueo temporal: 5 por correo+IP o 20 por IP en 15 minutos). */
public final class LoginIntentoDao {

    private LoginIntentoDao() {
    }

    public static long fallosDeCuenta(String ip, String email) {
        return Db.scalar("SELECT COUNT(*) FROM login_intentos WHERE ip = ? AND email = ? AND fecha > ?", ip, email, Fechas.ahora().minusMinutes(15));
    }

    public static long fallosDeIp(String ip) {
        return Db.scalar("SELECT COUNT(*) FROM login_intentos WHERE ip = ? AND fecha > ?", ip, Fechas.ahora().minusMinutes(15));
    }

    public static void registrar(String email, String ip) {
        Db.update("INSERT INTO login_intentos (email, ip, fecha) VALUES (?, ?, ?)", email, ip, Fechas.ahora());
    }

    /** Tras un login correcto: borra los intentos de esa cuenta+IP y los registros de más de un día. */
    public static void limpiar(String ip, String email) {
        Db.update("DELETE FROM login_intentos WHERE ip = ? AND email = ?", ip, email);
        Db.update("DELETE FROM login_intentos WHERE fecha < ?", Fechas.ahora().minusDays(1));
    }
}
