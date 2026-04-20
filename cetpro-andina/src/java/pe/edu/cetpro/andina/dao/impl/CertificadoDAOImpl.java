package pe.edu.cetpro.andina.dao.impl;

import pe.edu.cetpro.andina.config.DatabaseConnection;
import pe.edu.cetpro.andina.dao.CertificadoDAO;
import pe.edu.cetpro.andina.entity.Certificado;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CertificadoDAOImpl implements CertificadoDAO {

    private static final Logger LOGGER = Logger.getLogger(CertificadoDAOImpl.class.getName());
    private final DatabaseConnection db = DatabaseConnection.getInstance();

    @Override
    public Certificado insertar(Certificado c) {
        String sql = "INSERT INTO certificados (id_matricula, tipo_certificado, numero_correlativo, " +
                "codigo_institucional, numero_registro, anio_emision, fecha_emision, lugar_emision, " +
                "nota_final, nota_final_texto, duracion_horas, promedio_final, observaciones, id_usuario_emisor) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?) RETURNING id_certificado, fecha_creacion";
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, c.getIdMatricula());
            ps.setString(2, c.getTipoCertificado().name());
            ps.setString(3, c.getNumeroCorrelativo());
            ps.setString(4, c.getCodigoInstitucional());
            ps.setString(5, c.getNumeroRegistro());
            ps.setInt(6, c.getAnioEmision());
            ps.setDate(7, Date.valueOf(c.getFechaEmision()));
            ps.setString(8, c.getLugarEmision());
            if (c.getNotaFinal() != null) ps.setInt(9, c.getNotaFinal()); else ps.setNull(9, Types.INTEGER);
            ps.setString(10, c.getNotaFinalTexto());
            if (c.getDuracionHoras() != null) ps.setInt(11, c.getDuracionHoras()); else ps.setNull(11, Types.INTEGER);
            if (c.getPromedioFinal() != null) ps.setBigDecimal(12, c.getPromedioFinal()); else ps.setNull(12, Types.NUMERIC);
            ps.setString(13, c.getObservaciones());
            if (c.getIdUsuarioEmisor() != null) ps.setInt(14, c.getIdUsuarioEmisor()); else ps.setNull(14, Types.INTEGER);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    c.setIdCertificado(rs.getInt("id_certificado"));
                    c.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
                }
            }
            registrarHistorial(con, c.getIdCertificado(), "CREADO",
                    "Certificado emitido: " + c.getCodigoInstitucional());
            return c;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error insertar certificado", e);
            throw new RuntimeException("No se pudo emitir el certificado: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean actualizar(Certificado c) {
        String sql = "UPDATE certificados SET nota_final=?, nota_final_texto=?, duracion_horas=?, " +
                "promedio_final=?, observaciones=?, estado=? WHERE id_certificado=?";
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (c.getNotaFinal() != null) ps.setInt(1, c.getNotaFinal()); else ps.setNull(1, Types.INTEGER);
            ps.setString(2, c.getNotaFinalTexto());
            if (c.getDuracionHoras() != null) ps.setInt(3, c.getDuracionHoras()); else ps.setNull(3, Types.INTEGER);
            if (c.getPromedioFinal() != null) ps.setBigDecimal(4, c.getPromedioFinal()); else ps.setNull(4, Types.NUMERIC);
            ps.setString(5, c.getObservaciones());
            ps.setString(6, c.getEstado().name());
            ps.setInt(7, c.getIdCertificado());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error actualizar certificado", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean eliminar(Integer id) {
        // Nunca borrar físicamente: anular
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE certificados SET estado='ANULADO' WHERE id_certificado=?")) {
            ps.setInt(1, id);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) registrarHistorial(con, id, "ANULADO", "Certificado anulado");
            return ok;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Certificado> buscarPorId(Integer id) {
        String sql = "SELECT * FROM v_certificados_completo WHERE id_certificado = ?";
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapearCompleto(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error buscar certificado", e);
            return Optional.empty();
        }
    }

    @Override
    public List<Certificado> listarTodos() {
        return listarCompleto();
    }

    @Override
    public List<Certificado> listarCompleto() {
        String sql = "SELECT * FROM v_certificados_completo ORDER BY fecha_emision DESC, id_certificado DESC";
        return ejecutarLista(sql);
    }

    @Override
    public List<Certificado> listarPorAlumno(Integer idAlumno) {
        String sql = "SELECT * FROM v_certificados_completo WHERE id_alumno = ? ORDER BY fecha_emision DESC";
        List<Certificado> lista = new ArrayList<>();
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idAlumno);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearCompleto(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error listar por alumno", e);
        }
        return lista;
    }

    @Override
    public List<Certificado> filtrar(Certificado.Tipo tipo, Integer anio, Boolean entregado) {
        StringBuilder sb = new StringBuilder("SELECT * FROM v_certificados_completo WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (tipo != null) { sb.append("AND tipo_certificado = ? "); params.add(tipo.name()); }
        if (anio != null) { sb.append("AND anio_emision = ? "); params.add(anio); }
        if (entregado != null) { sb.append("AND entregado = ? "); params.add(entregado); }
        sb.append("ORDER BY fecha_emision DESC");
        List<Certificado> lista = new ArrayList<>();
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearCompleto(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error filtrar certificados", e);
        }
        return lista;
    }

    @Override
    public boolean marcarImpreso(Integer idCertificado, boolean impreso) {
        String sql = "UPDATE certificados SET impreso = ?, fecha_impresion = " +
                (impreso ? "CURRENT_TIMESTAMP" : "NULL") + " WHERE id_certificado = ?";
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, impreso);
            ps.setInt(2, idCertificado);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) registrarHistorial(con, idCertificado,
                    impreso ? "IMPRESO" : "IMPRESION_ANULADA",
                    impreso ? "Marcado como impreso" : "Se desmarcó impresión");
            return ok;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error marcar impreso", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean marcarEntregado(Integer idCertificado, boolean entregado, String firmaReceptor) {
        String sql = "UPDATE certificados SET entregado = ?, " +
                "fecha_entrega = " + (entregado ? "CURRENT_TIMESTAMP" : "NULL") + ", " +
                "firma_receptor = ? WHERE id_certificado = ?";
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, entregado);
            ps.setString(2, entregado ? firmaReceptor : null);
            ps.setInt(3, idCertificado);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) registrarHistorial(con, idCertificado,
                    entregado ? "ENTREGADO" : "ENTREGA_ANULADA",
                    entregado ? ("Entregado a: " + firmaReceptor) : "Se desmarcó entrega");
            return ok;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error marcar entregado", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean existeCodigoInstitucional(String codigo) {
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT 1 FROM certificados WHERE codigo_institucional = ?")) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int siguienteCorrelativo(Certificado.Tipo tipo, int anio) {
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT fn_siguiente_correlativo(?, ?)")) {
            ps.setString(1, tipo.name());
            ps.setInt(2, anio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
            throw new SQLException("Sin correlativo");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error correlativo", e);
            throw new RuntimeException(e);
        }
    }

    // ============ helpers ============
    private List<Certificado> ejecutarLista(String sql) {
        List<Certificado> lista = new ArrayList<>();
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearCompleto(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error listar", e);
        }
        return lista;
    }

    private void registrarHistorial(Connection con, Integer idCert, String accion, String detalle) {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO historial_certificados (id_certificado, accion, detalle) VALUES (?,?,?)")) {
            ps.setInt(1, idCert);
            ps.setString(2, accion);
            ps.setString(3, detalle);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "No se registró historial", e);
        }
    }

    private Certificado mapearCompleto(ResultSet rs) throws SQLException {
        Certificado c = new Certificado();
        c.setIdCertificado(rs.getInt("id_certificado"));
        c.setIdMatricula(rs.getInt("id_matricula"));
        c.setCodigoInstitucional(rs.getString("codigo_institucional"));
        c.setNumeroCorrelativo(rs.getString("numero_correlativo"));
        c.setNumeroRegistro(rs.getString("numero_registro"));
        c.setTipoCertificado(Certificado.Tipo.valueOf(rs.getString("tipo_certificado")));
        c.setFechaEmision(rs.getDate("fecha_emision").toLocalDate());
        c.setAnioEmision(rs.getInt("anio_emision"));
        c.setLugarEmision(rs.getString("lugar_emision"));
        int nota = rs.getInt("nota_final");
        if (!rs.wasNull()) c.setNotaFinal(nota);
        c.setNotaFinalTexto(rs.getString("nota_final_texto"));
        int dur = rs.getInt("duracion_horas");
        if (!rs.wasNull()) c.setDuracionHoras(dur);
        c.setPromedioFinal(rs.getBigDecimal("promedio_final"));
        c.setImpreso(rs.getBoolean("impreso"));
        Timestamp fi_imp = rs.getTimestamp("fecha_impresion");
        if (fi_imp != null) c.setFechaImpresion(fi_imp.toLocalDateTime());
        c.setEntregado(rs.getBoolean("entregado"));
        Timestamp fe = rs.getTimestamp("fecha_entrega");
        if (fe != null) c.setFechaEntrega(fe.toLocalDateTime());
        c.setFirmaReceptor(rs.getString("firma_receptor"));
        c.setEstado(Certificado.Estado.valueOf(rs.getString("estado")));
        c.setObservaciones(rs.getString("observaciones"));
        Timestamp fc = rs.getTimestamp("fecha_creacion");
        if (fc != null) c.setFechaCreacion(fc.toLocalDateTime());
        c.setAlumnoDni(rs.getString("dni"));
        c.setAlumnoNombre(rs.getString("alumno_nombre"));
        c.setModuloCodigo(rs.getString("codigo_modulo"));
        c.setModuloNombre(rs.getString("modulo_nombre"));
        c.setProgramaNombre(rs.getString("programa_nombre"));
        c.setCicloFormativo(rs.getString("ciclo_formativo"));
        Date fic = rs.getDate("fecha_inicio");
        if (fic != null) c.setFechaInicioCurso(fic.toLocalDate());
        Date ff = rs.getDate("fecha_fin");
        if (ff != null) c.setFechaFinCurso(ff.toLocalDate());
        return c;
    }
}
