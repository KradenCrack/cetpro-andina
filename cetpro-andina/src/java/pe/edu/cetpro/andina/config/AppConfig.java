package pe.edu.cetpro.andina.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Carga configuración desde database.properties.
 * Permite cambiar credenciales sin tocar código (preparado para Ubuntu).
 *
 * En producción (Ubuntu), sobrescribir con variables de entorno:
 *   export DB_HOST=...
 *   export DB_USER=...
 *   export DB_PASSWORD=...
 */
public class AppConfig {

    private static final Properties props = new Properties();

    static {
        try (InputStream in = AppConfig.class.getClassLoader()
                .getResourceAsStream("database.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar database.properties", e);
        }
    }

    public static String get(String key) {
        // Prioridad: variable de entorno > properties
        String envVar = "DB_" + key.toUpperCase().replace(".", "_");
        String envValue = System.getenv(envVar);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        return props.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        String value = get(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    public static int getInt(String key, int defaultValue) {
        String value = get(key);
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private AppConfig() {} // utility class
}
