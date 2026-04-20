package pe.edu.cetpro.andina.dao;

import pe.edu.cetpro.andina.entity.UnidadDidactica;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MatriculaDAO {

    Integer crear(Integer idAlumno, Integer idModulo, LocalDate inicio, LocalDate fin, String ciclo);

    Optional<Integer> buscar(Integer idAlumno, Integer idModulo, String ciclo);

    /** Guarda notas por unidad didáctica. */
    void guardarNotas(Integer idMatricula, List<UnidadDidactica> unidadesConNota);

    /** Lista unidades con sus notas. */
    List<UnidadDidactica> listarUnidadesConNotas(Integer idMatricula);

    /** Calcula promedio ponderado por créditos. */
    double calcularPromedio(Integer idMatricula);
}
