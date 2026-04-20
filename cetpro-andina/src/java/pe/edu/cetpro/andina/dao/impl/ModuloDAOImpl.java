package pe.edu.cetpro.andina.dao.impl;

import pe.edu.cetpro.andina.config.DatabaseConnection;
import pe.edu.cetpro.andina.dao.ModuloDAO;
import pe.edu.cetpro.andina.entity.Modulo;
import pe.edu.cetpro.andina.entity.UnidadDidactica;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ModuloDAOImpl implements ModuloDAO {

    private static final Logger LOGGER = Logger.getLogger(ModuloDAOImpl.class.getName());
    private final DatabaseConnection db = DatabaseConnection.getInstance();

    @Override
    public Modulo insertar(Modulo m) {
        String sql = "INSERT INTO modulos (id_programa, codigo_modulo, nombre, total_creditos, " +
                "total_horas, ciclo_formativo, modalidad) VALUES (?,?,?,?,?,?,?) RETURNING id_modulo";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, m.getIdPrograma());
            ps.setString(2, m.getCodigoModulo());
            ps.setString(3, m.getNombre());
            ps.setInt(4, m.getTotalCreditos());
            ps.setInt(5, m.getTotalHoras());
            ps.setString(6, m.getCicloFormativo());
            ps.setString(7, m.getModalidad());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) m.setIdModulo(rs.getInt(1));
            }
            return m;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error insertar módulo", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean actualizar(Modulo m) {
        String sql = "UPDATE modulos SET nombre=?, total_creditos=?, total_horas=?, " +
                "ciclo_formativo=?, modalidad=? WHERE id_modulo=?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, m.getNombre());
            ps.setInt(2, m.getTotalCreditos());
            ps.setInt(3, m.getTotalHoras());
            ps.setString(4, m.getCicloFormativo());
            ps.setString(5, m.getModalidad());
            ps.setInt(6, m.getIdModulo());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error actualizar módulo", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean eliminar(Integer id) {
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE modulos SET activo=FALSE WHERE id_modulo=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Modulo> buscarPorId(Integer id) {
        String sql = "SELECT m.*, p.nombre AS programa_nombre FROM modulos m " +
                "JOIN programas_estudio p ON p.id_programa = m.id_programa " +
                "WHERE m.id_modulo = ? AND m.activo = TRUE";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error buscar módulo", e);
            return Optional.empty();
        }
    }

    @Override
    public List<Modulo> listarTodos() {
        String sql = "SELECT m.*, p.nombre AS programa_nombre FROM modulos m " +
                "JOIN programas_estudio p ON p.id_programa = m.id_programa " +
                "WHERE m.activo = TRUE ORDER BY p.nombre, m.codigo_modulo";
        List<Modulo> lista = new ArrayList<>();
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error listar módulos", e);
        }
        return lista;
    }

    @Override
    public List<Modulo> listarPorPrograma(Integer idPrograma) {
        String sql = "SELECT m.*, p.nombre AS programa_nombre FROM modulos m " +
                "JOIN programas_estudio p ON p.id_programa = m.id_programa " +
                "WHERE m.id_programa = ? AND m.activo = TRUE ORDER BY m.codigo_modulo";
        List<Modulo> lista = new ArrayList<>();
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idPrograma);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error listar por programa", e);
        }
        return lista;
    }

    @Override
    public Optional<Modulo> buscarConUnidades(Integer idModulo) {
        Optional<Modulo> opt = buscarPorId(idModulo);
        opt.ifPresent(m -> m.setUnidades(listarUnidades(idModulo)));
        return opt;
    }

    @Override
    public List<UnidadDidactica> listarUnidades(Integer idModulo) {
        String sql = "SELECT * FROM unidades_didacticas WHERE id_modulo = ? " +
                "AND activo = TRUE ORDER BY orden";
        List<UnidadDidactica> lista = new ArrayList<>();
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idModulo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UnidadDidactica u = new UnidadDidactica();
                    u.setIdUnidad(rs.getInt("id_unidad"));
                    u.setIdModulo(rs.getInt("id_modulo"));
                    u.setNombre(rs.getString("nombre"));
                    u.setCreditos(rs.getInt("creditos"));
                    u.setHoras(rs.getInt("horas"));
                    u.setCapacidades(rs.getString("capacidades"));
                    u.setUnidadCompetencia(rs.getString("unidad_competencia"));
                    u.setOrden(rs.getInt("orden"));
                    lista.add(u);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error listar unidades", e);
        }
        return lista;
    }

    private Modulo mapear(ResultSet rs) throws SQLException {
        Modulo m = new Modulo();
        m.setIdModulo(rs.getInt("id_modulo"));
        m.setIdPrograma(rs.getInt("id_programa"));
        m.setCodigoModulo(rs.getString("codigo_modulo"));
        m.setNombre(rs.getString("nombre"));
        m.setTotalCreditos(rs.getInt("total_creditos"));
        m.setTotalHoras(rs.getInt("total_horas"));
        m.setCicloFormativo(rs.getString("ciclo_formativo"));
        m.setModalidad(rs.getString("modalidad"));
        try { m.setNombrePrograma(rs.getString("programa_nombre")); } catch (SQLException ignored) {}
        return m;
    }
}
