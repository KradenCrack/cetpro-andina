package pe.edu.cetpro.andina.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestor de conexiones a PostgreSQL.
 *
 * Patrón Singleton THREAD-SAFE (double-checked locking).
 * Cada llamada a {@link #getConnection()} devuelve una conexión nueva
 * (NO reutiliza la misma para evitar conflictos en múltiples requests HTTP).
 *
 * Para entornos con mucha carga se recomienda migrar a un pool de
 * conexiones como HikariCP (ver comentario al final). Esta implementación
 * es suficiente para la fase 1 y el despliegue inicial en Ubuntu.
 *
 * Aplica SOLID:
 *  - SRP: solo responsabilidad de gestionar conexiones.
 *  - DIP: los DAOs dependen de esta abstracción, no de JDBC directamente.
 */
public class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    // Volatile asegura visibilidad del singleton entre threads
    private static volatile DatabaseConnection instance;

    private final String url;
    private final String user;
    private final String password;

    private DatabaseConnection() {
        String host = AppConfig.get("db.host", "localhost");
        String port = AppConfig.get("db.port", "5432");
        String name = AppConfig.get("db.name", "cetpro_andina");

        this.url      = String.format("jdbc:postgresql://%s:%s/%s", host, port, name);
        this.user     = AppConfig.get("db.user", "postgres");
        this.password = AppConfig.get("db.password", "");

        // Registrar driver (opcional en JDBC 4+, pero explícito por claridad)
        try {
            Class.forName("org.postgresql.Driver");
            LOGGER.info("Driver PostgreSQL registrado correctamente");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Driver PostgreSQL no encontrado en el classpath", e);
            throw new RuntimeException("Agregar postgresql-xx.x.x.jar a las librerías del proyecto", e);
        }
    }

    /**
     * Devuelve la instancia única del gestor (thread-safe).
     */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    /**
     * Devuelve una conexión nueva. El llamador es responsable de cerrarla
     * (usar try-with-resources).
     */
    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(url, user, password);
        conn.setAutoCommit(true); // por defecto; los servicios lo desactivan en transacciones
        return conn;
    }

    /**
     * Verifica conectividad. Útil al arrancar la app.
     */
    public boolean testConnection() {
        try (Connection c = getConnection()) {
            return c != null && !c.isClosed();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Fallo al conectar: " + e.getMessage(), e);
            return false;
        }
    }

    /*
     * ------------------------------------------------------------------
     *  MIGRACIÓN FUTURA A HIKARICP (Fase 2, despliegue Ubuntu)
     * ------------------------------------------------------------------
     *  Cuando la carga crezca, reemplazar DriverManager por un DataSource:
     *
     *  private HikariDataSource dataSource;
     *
     *  private DatabaseConnection() {
     *      HikariConfig config = new HikariConfig();
     *      config.setJdbcUrl(url);
     *      config.setUsername(user);
     *      config.setPassword(password);
     *      config.setMaximumPoolSize(AppConfig.getInt("db.pool.max", 10));
     *      config.setConnectionTimeout(AppConfig.getInt("db.pool.timeout", 30000));
     *      this.dataSource = new HikariDataSource(config);
     *  }
     *  public Connection getConnection() throws SQLException {
     *      return dataSource.getConnection();
     *  }
     *
     *  Esto NO romperá los DAOs porque siguen llamando getConnection().
     * ------------------------------------------------------------------
     */
}
