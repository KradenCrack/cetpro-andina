package pe.edu.cetpro.andina.dao;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz genérica para operaciones CRUD básicas.
 *
 * Aplica:
 *  - DIP (Dependency Inversion): los servicios dependen de esta abstracción,
 *    no de implementaciones concretas.
 *  - LSP (Liskov Substitution): cualquier DAO puede sustituirse por otro
 *    que implemente la misma interfaz (Ej: mañana un MongoAlumnoDAO).
 *  - OCP (Open/Closed): para añadir nuevas entidades se crean nuevos DAOs
 *    sin tocar los existentes.
 *
 * @param <T>  Tipo de la entidad
 * @param <ID> Tipo de la clave primaria
 */
public interface GenericDAO<T, ID> {

    /** Inserta y devuelve la entidad con ID asignado. */
    T insertar(T entidad);

    /** Actualiza. Devuelve true si modificó al menos una fila. */
    boolean actualizar(T entidad);

    /** Borrado lógico (setea activo=false) o físico según implementación. */
    boolean eliminar(ID id);

    /** Busca por clave primaria. */
    Optional<T> buscarPorId(ID id);

    /** Lista todos los registros activos. */
    List<T> listarTodos();
}
