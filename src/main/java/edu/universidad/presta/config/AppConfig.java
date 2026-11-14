package edu.universidad.presta.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.Properties;

/**
 * Configuración de la aplicación. Lee /app.properties y permite sobrescribir cada valor
 * con variables de entorno (útil para Docker o para no guardar la contraseña en el código).
 */
public final class AppConfig {

    public static final String SITE_NAME = "Sistema de Préstamo de Equipos";

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = AppConfig.class.getResourceAsStream("/app.properties")) {
            if (in != null) {
                PROPS.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private AppConfig() {
    }

    private static String get(String clave, String variableEntorno, String porDefecto) {
        String v = System.getenv(variableEntorno);
        if (v == null || v.isBlank()) {
            v = PROPS.getProperty(clave);
        }
        return v != null ? v : porDefecto;
    }

    public static String dbHost() {
        return get("db.host", "PRESTAMOS_DB_HOST", "localhost").trim();
    }

    public static int dbPort() {
        return Integer.parseInt(get("db.port", "PRESTAMOS_DB_PORT", "1433").trim());
    }

    public static String dbName() {
        return get("db.name", "PRESTAMOS_DB_NAME", "prestamos_equipos").trim();
    }

    public static String dbUser() {
        return get("db.user", "PRESTAMOS_DB_USER", "sa").trim();
    }

    /** La contraseña no se recorta. Mejor definirla con la variable de entorno PRESTAMOS_DB_PASS. */
    public static String dbPassword() {
        return get("db.password", "PRESTAMOS_DB_PASS", "");
    }

    /** Token (API key) del servicio de consulta de DNI (Decolecta / RENIEC). Vacío = solo se usa el proveedor de respaldo. */
    public static String dniApiToken() {
        return get("dni.api.token", "PRESTAMOS_DNI_TOKEN", "").trim();
    }

    /** true = confía en el certificado del servidor (SQL Server local usa uno autofirmado). */
    public static boolean dbTrustCertificate() {
        return Boolean.parseBoolean(get("db.trustServerCertificate", "PRESTAMOS_DB_TRUST_CERT", "true").trim());
    }

    /** Máximo de conexiones abiertas a la vez en el pool (Conexion). */
    public static int dbPoolSize() {
        return Math.max(1, Integer.parseInt(get("db.poolSize", "PRESTAMOS_DB_POOL", "10").trim()));
    }

    /** Clave del proveedor de IA; solo se lee en el servidor desde variables de entorno. */
    public static String aiApiKey() {
        return get("ai.api.key", "PRESTAMOS_AI_API_KEY", "").trim();
    }

    /** Modelo configurable para el proveedor compatible con Chat Completions. */
    public static String aiModel() {
        return get("ai.model", "PRESTAMOS_AI_MODEL", "gpt-5.6-luna").trim();
    }

    public static String aiEndpoint() {
        return get("ai.endpoint", "PRESTAMOS_AI_ENDPOINT", "https://api.openai.com/v1/chat/completions").trim();
    }

    public static int aiTimeoutSeconds() {
        return Math.max(10, Math.min(60, Integer.parseInt(get("ai.timeout.seconds", "PRESTAMOS_AI_TIMEOUT", "35").trim())));
    }

    /** Zona horaria de la aplicación (America/Lima por defecto). */
    public static ZoneId zone() {
        return ZoneId.of(get("app.timezone", "PRESTAMOS_TZ", "America/Lima").trim());
    }
}
