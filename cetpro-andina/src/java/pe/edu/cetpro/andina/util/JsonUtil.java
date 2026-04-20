package pe.edu.cetpro.andina.util;

import pe.edu.cetpro.andina.entity.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** JSON manual (sin dependencias externas). */
public class JsonUtil {

    public static String escape(String s) {
        if (s == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int)c));
                    else sb.append(c);
            }
        }
        return sb.append("\"").toString();
    }

    private static String v(Object o) {
        if (o == null) return "null";
        if (o instanceof Number || o instanceof Boolean) return o.toString();
        if (o instanceof LocalDate || o instanceof LocalDateTime) return escape(o.toString());
        return escape(o.toString());
    }

    public static String alumnoToJson(Alumno a) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"idAlumno\":").append(v(a.getIdAlumno())).append(",");
        sb.append("\"dni\":").append(v(a.getDni())).append(",");
        sb.append("\"tipoDocumento\":").append(v(a.getTipoDocumento())).append(",");
        sb.append("\"nombres\":").append(v(a.getNombres())).append(",");
        sb.append("\"apellidoPaterno\":").append(v(a.getApellidoPaterno())).append(",");
        sb.append("\"apellidoMaterno\":").append(v(a.getApellidoMaterno())).append(",");
        sb.append("\"nombreCompleto\":").append(v(a.getNombreCompleto())).append(",");
        sb.append("\"telefono\":").append(v(a.getTelefono())).append(",");
        sb.append("\"email\":").append(v(a.getEmail())).append(",");
        sb.append("\"direccion\":").append(v(a.getDireccion())).append(",");
        sb.append("\"totalCertificados\":").append(v(a.getTotalCertificados())).append(",");
        sb.append("\"certificadosEntregados\":").append(v(a.getCertificadosEntregados())).append(",");
        sb.append("\"certificadosPendientes\":").append(v(a.getCertificadosPendientes()));
        return sb.append("}").toString();
    }

    public static String certificadoToJson(Certificado c) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"idCertificado\":").append(v(c.getIdCertificado())).append(",");
        sb.append("\"codigoInstitucional\":").append(v(c.getCodigoInstitucional())).append(",");
        sb.append("\"numeroCorrelativo\":").append(v(c.getNumeroCorrelativo())).append(",");
        sb.append("\"tipoCertificado\":").append(v(c.getTipoCertificado())).append(",");
        sb.append("\"fechaEmision\":").append(v(c.getFechaEmision())).append(",");
        sb.append("\"anioEmision\":").append(v(c.getAnioEmision())).append(",");
        sb.append("\"notaFinal\":").append(v(c.getNotaFinal())).append(",");
        sb.append("\"notaFinalTexto\":").append(v(c.getNotaFinalTexto())).append(",");
        sb.append("\"duracionHoras\":").append(v(c.getDuracionHoras())).append(",");
        sb.append("\"promedioFinal\":").append(v(c.getPromedioFinal())).append(",");
        sb.append("\"impreso\":").append(v(c.isImpreso())).append(",");
        sb.append("\"entregado\":").append(v(c.isEntregado())).append(",");
        sb.append("\"fechaEntrega\":").append(v(c.getFechaEntrega())).append(",");
        sb.append("\"estado\":").append(v(c.getEstado())).append(",");
        sb.append("\"alumnoDni\":").append(v(c.getAlumnoDni())).append(",");
        sb.append("\"alumnoNombre\":").append(v(c.getAlumnoNombre())).append(",");
        sb.append("\"moduloCodigo\":").append(v(c.getModuloCodigo())).append(",");
        sb.append("\"moduloNombre\":").append(v(c.getModuloNombre())).append(",");
        sb.append("\"programaNombre\":").append(v(c.getProgramaNombre())).append(",");
        sb.append("\"cicloFormativo\":").append(v(c.getCicloFormativo())).append(",");
        sb.append("\"fechaInicioCurso\":").append(v(c.getFechaInicioCurso())).append(",");
        sb.append("\"fechaFinCurso\":").append(v(c.getFechaFinCurso()));
        return sb.append("}").toString();
    }

    public static String moduloToJson(Modulo m) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"idModulo\":").append(v(m.getIdModulo())).append(",");
        sb.append("\"idPrograma\":").append(v(m.getIdPrograma())).append(",");
        sb.append("\"codigoModulo\":").append(v(m.getCodigoModulo())).append(",");
        sb.append("\"nombre\":").append(v(m.getNombre())).append(",");
        sb.append("\"nombrePrograma\":").append(v(m.getNombrePrograma())).append(",");
        sb.append("\"totalCreditos\":").append(v(m.getTotalCreditos())).append(",");
        sb.append("\"totalHoras\":").append(v(m.getTotalHoras())).append(",");
        sb.append("\"cicloFormativo\":").append(v(m.getCicloFormativo())).append(",");
        sb.append("\"modalidad\":").append(v(m.getModalidad())).append(",");
        sb.append("\"unidades\":[");
        List<UnidadDidactica> us = m.getUnidades();
        if (us != null) {
            for (int i = 0; i < us.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(unidadToJson(us.get(i)));
            }
        }
        return sb.append("]}").toString();
    }

    public static String unidadToJson(UnidadDidactica u) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"idUnidad\":").append(v(u.getIdUnidad())).append(",");
        sb.append("\"nombre\":").append(v(u.getNombre())).append(",");
        sb.append("\"creditos\":").append(v(u.getCreditos())).append(",");
        sb.append("\"horas\":").append(v(u.getHoras())).append(",");
        sb.append("\"capacidades\":").append(v(u.getCapacidades())).append(",");
        sb.append("\"orden\":").append(v(u.getOrden())).append(",");
        sb.append("\"calificacion\":").append(v(u.getCalificacion()));
        return sb.append("}").toString();
    }

    public static <T> String listToJson(List<T> list, java.util.function.Function<T,String> mapper) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(mapper.apply(list.get(i)));
        }
        return sb.append("]").toString();
    }

    public static String error(String msg) {
        return "{\"error\":" + escape(msg) + "}";
    }
    public static String ok(String msg) {
        return "{\"ok\":true,\"mensaje\":" + escape(msg) + "}";
    }

    private JsonUtil() {}
}
