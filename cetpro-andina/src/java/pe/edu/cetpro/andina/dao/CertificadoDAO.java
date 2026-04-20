package pe.edu.cetpro.andina.dao;

import pe.edu.cetpro.andina.entity.Certificado;

import java.util.List;
import java.util.Optional;

public interface CertificadoDAO extends GenericDAO<Certificado, Integer> {

    /** Lista certificados de un alumno específico. */
    List<Certificado> listarPorAlumno(Integer idAlumno);

    /** Marca como impreso (o desmarca). Actualiza fecha. */
    boolean marcarImpreso(Integer idCertificado, boolean impreso);

    /** Marca como entregado (o desmarca). Guarda firma y fecha. */
    boolean marcarEntregado(Integer idCertificado, boolean entregado, String firmaReceptor);

    /** Verifica si un código institucional ya existe (unicidad). */
    boolean existeCodigoInstitucional(String codigo);

    /**
     * Obtiene el siguiente número correlativo para un tipo+año.
     * Atómico: usa la función SQL fn_siguiente_correlativo.
     */
    int siguienteCorrelativo(Certificado.Tipo tipo, int anio);

    /** Listado completo con JOINs (vista v_certificados_completo). */
    List<Certificado> listarCompleto();

    /** Filtros: tipo, año, entregado */
    List<Certificado> filtrar(Certificado.Tipo tipo, Integer anio, Boolean entregado);
}
