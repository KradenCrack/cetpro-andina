package pe.edu.cetpro.andina.service;

import pe.edu.cetpro.andina.dao.AlumnoDAO;
import pe.edu.cetpro.andina.dao.impl.AlumnoDAOImpl;
import pe.edu.cetpro.andina.entity.Alumno;

import java.util.List;
import java.util.Optional;

public class AlumnoService {

    private final AlumnoDAO dao;

    public AlumnoService() { this(new AlumnoDAOImpl()); }
    public AlumnoService(AlumnoDAO dao) { this.dao = dao; }

    public Alumno registrar(Alumno a) {
        validar(a);
        if (dao.buscarPorDni(a.getDni()).isPresent())
            throw new IllegalArgumentException("Ya existe alumno con DNI " + a.getDni());
        return dao.insertar(a);
    }

    public boolean actualizar(Alumno a) {
        validar(a);
        return dao.actualizar(a);
    }

    public Optional<Alumno> buscar(Integer id) { return dao.buscarPorId(id); }
    public Optional<Alumno> buscarPorDni(String dni) { return dao.buscarPorDni(dni); }
    public List<Alumno> buscarPorNombre(String q) { return dao.buscarPorNombre(q); }
    public List<Alumno> listar() { return dao.listarTodos(); }
    public List<Alumno> listarConEstado() { return dao.listarConEstadoCertificados(); }
    public boolean eliminar(Integer id) { return dao.eliminar(id); }

    private void validar(Alumno a) {
        if (a.getDni() == null || a.getDni().isBlank())
            throw new IllegalArgumentException("DNI requerido");
        if (a.getNombres() == null || a.getNombres().isBlank())
            throw new IllegalArgumentException("Nombres requeridos");
        if (a.getApellidoPaterno() == null || a.getApellidoPaterno().isBlank())
            throw new IllegalArgumentException("Apellido paterno requerido");
    }
}
