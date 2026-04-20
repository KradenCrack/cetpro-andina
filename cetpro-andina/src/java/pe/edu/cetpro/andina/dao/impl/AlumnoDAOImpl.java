package pe.edu.cetpro.andina.dao.impl;

import pe.edu.cetpro.andina.config.DatabaseConnection;
import pe.edu.cetpro.andina.dao.AlumnoDAO;
import pe.edu.cetpro.andina.entity.Alumno;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementación JDBC del AlumnoDAO.
 * Usa PreparedStatement (protege contra SQL injection).
 * try-with-resources garantiza cierre de recursos.
 */
public class AlumnoDAOImpl implements AlumnoDAO {

    private static final Logger LOGGER = Logger.getLogger(AlumnoDAOImpl.class.getName());
    private final DatabaseConnection db = DatabaseConnection.getInstance();

    @Override
    public Alumno insertar(Alumno a) {
        String sql = "INSERT INTO alumnos (dni, tipo_documento, nombres, apellido_paterno, " +
                "apellido_materno, fecha_nacimiento, genero, telefono, email, direccion) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?) RETURNING id_alumno, fecha_registro";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, a.getDni());
            ps.setString(2, a.getTipoDocumento() != null ? a.getTipoDocumento() : "DNI");
            ps.setString(3, a.getNombres());
            ps.setString(4, a.getApellidoPaterno());
            ps.setString(5, a.getApellidoMaterno());
            ps.setDate(6, a.getFechaNacimiento() != null ? Date.valueOf(a.getFechaNacimiento()) : null);
            ps.setString(7, a.getGenero());
            ps.setString(8, a.getTelefono());
            ps.setString(9, a.getEmail());
            ps.setString(10, a.getDireccion());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    a.setIdAlumno(rs.getInt("id_alumno"));
                    a.setFechaRegistro(rs.getTimestamp("fecha_registro").toLocalDateTime());
                }
            }
            return a;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al insertar alumno", e);
            throw new RuntimeException("No se pudo registrar el alumno: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean actualizar(Alumno a) {
        String sql = "UPDATE alumnos SET dni=?, tipo_documento=?, nombres=?, apellido_paterno=?, " +
                "apellido_materno=?, fecha_nacimiento=?, genero=?, telefono=?, email=?, direccion=? " +
                "WHERE id_alumno=?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, a.getDni());
            ps.setString(2, a.getTipoDocumento());
            ps.setString(3, a.getNombres());
            ps.setString(4, a.getApellidoPaterno());
            ps.setString(5, a.getApellidoMaterno());
            ps.setDate(6, a.getFechaNacimiento() != null ? Date.valueOf(a.getFechaNacimiento()) : null);
            ps.setString(7, a.getGenero());
            ps.setString(8, a.getTelefono());
            ps.setString(9, a.getEmail());
            ps.setString(10, a.getDireccion());
            ps.setInt(11, a.getIdAlumno());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar alumno", e);
            throw new RuntimeException("No se pudo actualizar el alumno", e);
        }
    }

    @Override
    public boolean eliminar(Integer id) {
        // Borrado lógico
        String sql = "UPDATE alumnos SET activo = FALSE WHERE id_alumno = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar alumno", e);
            throw new RuntimeException("No se pudo eliminar el alumno", e);
        }
    }

    @Override
    public Optional<Alumno> buscarPorId(Integer id) {
        String sql = "SELECT * FROM alumnos WHERE id_alumno = ? AND activo = TRUE";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar alumno", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Alumno> buscarPorDni(String dni) {
        String sql = "SELECT * FROM alumnos WHERE dni = ? AND activo = TRUE";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, dni);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar por DNI", e);
            return Optional.empty();
        }
    }

    @Override
    public List<Alumno> buscarPorNombre(String criterio) {
        String sql = "SELECT * FROM alumnos WHERE activo = TRUE AND (" +
                "LOWER(nombres) LIKE LOWER(?) OR " +
                "LOWER(apellido_paterno) LIKE LOWER(?) OR " +
                "LOWER(apellido_materno) LIKE LOWER(?) OR " +
                "dni LIKE ?) ORDER BY apellido_paterno LIMIT 50";
        List<Alumno> lista = new ArrayList<>();
        String param = "%" + criterio + "%";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, param);
            ps.setString(2, param);
            ps.setString(3, param);
            ps.setString(4, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en búsqueda por nombre", e);
        }
        return lista;
    }

    @Override
    public List<Alumno> listarTodos() {
        String sql = "SELECT * FROM alumnos WHERE activo = TRUE ORDER BY apellido_paterno, nombres";
        List<Alumno> lista = new ArrayList<>();
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar alumnos", e);
        }
        return lista;
    }

    @Override
    public List<Alumno> listarConEstadoCertificados() {
        String sql = "SELECT * FROM v_alumnos_certificados ORDER BY apellido_paterno, nombres";
        List<Alumno> lista = new ArrayList<>();
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Alumno a = mapear(rs);
                a.setTotalCertificados(rs.getInt("total_certificados"));
                a.setCertificadosEntregados(rs.getInt("certificados_entregados"));
                a.setCertificadosPendientes(rs.getInt("certificados_pendientes"));
                lista.add(a);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar con estado", e);
        }
        return lista;
    }

    /** Mapea un ResultSet a la entidad Alumno. */
    private Alumno mapear(ResultSet rs) throws SQLException {
        Alumno a = new Alumno();
        a.setIdAlumno(rs.getInt("id_alumno"));
        a.setDni(rs.getString("dni"));
        a.setTipoDocumento(rs.getString("tipo_documento"));
        a.setNombres(rs.getString("nombres"));
        a.setApellidoPaterno(rs.getString("apellido_paterno"));
        a.setApellidoMaterno(rs.getString("apellido_materno"));
        Date fn = rs.getDate("fecha_nacimiento");
        if (fn != null) a.setFechaNacimiento(fn.toLocalDate());
        a.setGenero(rs.getString("genero"));
        a.setTelefono(rs.getString("telefono"));
        a.setEmail(rs.getString("email"));
        a.setDireccion(rs.getString("direccion"));
        a.setActivo(rs.getBoolean("activo"));
        Timestamp fr = rs.getTimestamp("fecha_registro");
        if (fr != null) a.setFechaRegistro(fr.toLocalDateTime());
        return a;
    }
}
