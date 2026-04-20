package pe.edu.cetpro.andina.service;

import pe.edu.cetpro.andina.dao.ModuloDAO;
import pe.edu.cetpro.andina.dao.impl.ModuloDAOImpl;
import pe.edu.cetpro.andina.entity.Modulo;
import pe.edu.cetpro.andina.entity.UnidadDidactica;

import java.util.List;
import java.util.Optional;

public class ModuloService {
    private final ModuloDAO dao;
    public ModuloService() { this(new ModuloDAOImpl()); }
    public ModuloService(ModuloDAO d) { this.dao = d; }

    public List<Modulo> listar() { return dao.listarTodos(); }
    public List<Modulo> listarPorPrograma(Integer id) { return dao.listarPorPrograma(id); }
    public Optional<Modulo> buscarConUnidades(Integer id) { return dao.buscarConUnidades(id); }
    public List<UnidadDidactica> listarUnidades(Integer id) { return dao.listarUnidades(id); }
}
