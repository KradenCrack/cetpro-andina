package pe.edu.cetpro.andina.dao;

import pe.edu.cetpro.andina.entity.Alumno;

import java.util.List;
import java.util.Optional;

public interface AlumnoDAO extends GenericDAO<Alumno, Integer> {

    /** Busca un alumno por su DNI/CE/Pasaporte. */
    Optional<Alumno> buscarPorDni(String dni);

    /** Búsqueda parcial por nombres o apellidos (para autocompletar). */
    List<Alumno> buscarPorNombre(String criterio);

    /**
     * Lista alumnos con datos agregados de certificados
     * (usa la vista v_alumnos_certificados para responder rápido).
     */
    List<Alumno> listarConEstadoCertificados();
}
