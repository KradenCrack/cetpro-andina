package pe.edu.cetpro.andina.dao;

import pe.edu.cetpro.andina.entity.Modulo;
import pe.edu.cetpro.andina.entity.UnidadDidactica;

import java.util.List;
import java.util.Optional;

public interface ModuloDAO extends GenericDAO<Modulo, Integer> {

    /** Lista módulos de un programa. */
    List<Modulo> listarPorPrograma(Integer idPrograma);

    /** Carga un módulo con sus unidades didácticas. */
    Optional<Modulo> buscarConUnidades(Integer idModulo);

    /** Lista unidades didácticas de un módulo. */
    List<UnidadDidactica> listarUnidades(Integer idModulo);
}
