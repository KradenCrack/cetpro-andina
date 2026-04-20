package pe.edu.cetpro.andina.dao.impl;

import pe.edu.cetpro.andina.config.DatabaseConnection;
import pe.edu.cetpro.andina.dao.MatriculaDAO;
import pe.edu.cetpro.andina.entity.UnidadDidactica;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MatriculaDAOImpl implements MatriculaDAO {

    private static final Logger LOGGER = Logger.getLogger(MatriculaDAOImpl.class.getName());
    private final DatabaseConnection db = DatabaseConnection.getInstance();

    @Override
    public Integer crear(Integer idAlumno, Integer idModulo, LocalDate inicio, LocalDate fin, String ciclo) {
        // Si ya existe, devolver el ID existente (idempotente)
        Optional<Integer> existente = buscar(idAlumno, idModulo, ciclo);
        if (existente.isPresent()) return existente.get();

        String sql = "INSERT INTO matriculas (id_alumno, id_modulo, fecha_inicio, fecha_fin, " +
                "ciclo_formativo, estado) VALUES (?,?,?,?,?,'APROBADO') RETURNING id_matricula";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idAlumno);
            ps.setInt(2, idModulo);
            ps.setDate(3, Date.valueOf(inicio));
            ps.setDate(4, Date.valueOf(fin));
            ps.setString(5, ciclo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
            throw new SQLException("No se obtuvo id_matricula");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error crear matrícula", e);
            throw new RuntimeException("No se pudo crear matrícula: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Integer> buscar(Integer idAlumno, Integer idModulo, String ciclo) {
        String sql = "SELECT id_matricula FROM matriculas WHERE id_alumno=? AND id_modulo=? AND ciclo_formativo=?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idAlumno);
            ps.setInt(2, idModulo);
            ps.setString(3, ciclo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(rs.getInt(1)) : Optional.empty();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error buscar matrícula", e);
            return Optional.empty();
        }
    }

    @Override
    public void guardarNotas(Integer idMatricula, List<UnidadDidactica> unidades) {
        // UPSERT: si existe actualiza, si no inserta
        String sql = "INSERT INTO notas (id_matricula, id_unidad, calificacion) VALUES (?,?,?) " +
                "ON CONFLICT (id_matricula, id_unidad) DO UPDATE SET " +
                "calificacion = EXCLUDED.calificacion, fecha_actualizacion = CURRENT_TIMESTAMP";
        try (Connection c = db.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                for (UnidadDidactica u : unidades) {
                    if (u.getCalificacion() == null) continue;
                    ps.setInt(1, idMatricula);
                    ps.setInt(2, u.getIdUnidad());
                    ps.setInt(3, u.getCalificacion());
                    ps.addBatch();
                }
                ps.executeBatch();
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error guardar notas", e);
            throw new RuntimeException("No se pudieron guardar las notas", e);
        }
    }

    @Override
    public List<UnidadDidactica> listarUnidadesConNotas(Integer idMatricula) {
        String sql = "SELECT u.*, n.calificacion FROM matriculas m " +
                "JOIN unidades_didacticas u ON u.id_modulo = m.id_modulo " +
                "LEFT JOIN notas n ON n.id_unidad = u.id_unidad AND n.id_matricula = m.id_matricula " +
                "WHERE m.id_matricula = ? AND u.activo = TRUE ORDER BY u.orden";
        List<UnidadDidactica> lista = new ArrayList<>();
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idMatricula);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UnidadDidactica u = new UnidadDidactica();
                    u.setIdUnidad(rs.getInt("id_unidad"));
                    u.setIdModulo(rs.getInt("id_modulo"));
                    u.setNombre(rs.getString("nombre"));
                    u.setCreditos(rs.getInt("creditos"));
                    u.setHoras(rs.getInt("horas"));
                    u.setCapacidades(rs.getString("capacidades"));
                    u.setOrden(rs.getInt("orden"));
                    int nota = rs.getInt("calificacion");
                    if (!rs.wasNull()) u.setCalificacion(nota);
                    lista.add(u);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error listar unidades con notas", e);
        }
        return lista;
    }

    @Override
    public double calcularPromedio(Integer idMatricula) {
        String sql = "SELECT SUM(n.calificacion * u.creditos)::numeric / NULLIF(SUM(u.creditos),0) AS promedio " +
                "FROM notas n JOIN unidades_didacticas u ON u.id_unidad = n.id_unidad " +
                "WHERE n.id_matricula = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idMatricula);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double prom = rs.getDouble("promedio");
                    return rs.wasNull() ? 0.0 : prom;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error calcular promedio", e);
        }
        return 0.0;
    }
}
