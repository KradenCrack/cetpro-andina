package pe.edu.cetpro.andina.service;

import pe.edu.cetpro.andina.dao.*;
import pe.edu.cetpro.andina.dao.impl.*;
import pe.edu.cetpro.andina.entity.*;
import pe.edu.cetpro.andina.util.GeneradorCodigos;
import pe.edu.cetpro.andina.util.NumeroATexto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Lógica de negocio de certificados.
 * SRP: orquesta DAOs, genera códigos, calcula promedios.
 * DIP: depende de interfaces DAO (inyectables).
 */
public class CertificadoService {

    private final CertificadoDAO certDao;
    private final AlumnoDAO alumnoDao;
    private final ModuloDAO moduloDao;
    private final MatriculaDAO matriculaDao;

    public CertificadoService() {
        this(new CertificadoDAOImpl(), new AlumnoDAOImpl(),
             new ModuloDAOImpl(), new MatriculaDAOImpl());
    }

    /** Constructor para inyección (tests). */
    public CertificadoService(CertificadoDAO c, AlumnoDAO a, ModuloDAO m, MatriculaDAO mat) {
        this.certDao = c; this.alumnoDao = a; this.moduloDao = m; this.matriculaDao = mat;
    }

    /**
     * Emite un certificado MODULAR.
     * Crea matrícula si no existe, guarda notas, calcula promedio, genera código.
     */
    public Certificado emitirModular(EmisionRequest req) {
        validar(req);

        Alumno alumno = alumnoDao.buscarPorId(req.idAlumno)
                .orElseThrow(() -> new IllegalArgumentException("Alumno no existe"));
        Modulo modulo = moduloDao.buscarConUnidades(req.idModulo)
                .orElseThrow(() -> new IllegalArgumentException("Módulo no existe"));

        // 1. Crear/obtener matrícula
        Integer idMat = matriculaDao.crear(req.idAlumno, req.idModulo,
                req.fechaInicioCurso, req.fechaFinCurso, req.cicloFormativo);

        // 2. Guardar notas por unidad
        if (req.notasPorUnidad != null && !req.notasPorUnidad.isEmpty()) {
            matriculaDao.guardarNotas(idMat, req.notasPorUnidad);
        }

        // 3. Calcular promedio ponderado
        double prom = matriculaDao.calcularPromedio(idMat);

        // 4. Generar código institucional
        int anio = req.fechaEmision.getYear();
        int corr = certDao.siguienteCorrelativo(Certificado.Tipo.MODULAR, anio);
        String codigo = GeneradorCodigos.generar(Certificado.Tipo.MODULAR,
                corr, anio, modulo.getCodigoModulo(), alumno.getDni());

        // 5. Crear certificado
        Certificado c = new Certificado();
        c.setIdMatricula(idMat);
        c.setTipoCertificado(Certificado.Tipo.MODULAR);
        c.setNumeroCorrelativo(String.format("%04d", corr));
        c.setCodigoInstitucional(codigo);
        c.setAnioEmision(anio);
        c.setFechaEmision(req.fechaEmision);
        c.setLugarEmision(req.lugarEmision != null ? req.lugarEmision : "Ica");
        c.setPromedioFinal(BigDecimal.valueOf(prom).setScale(2, RoundingMode.HALF_UP));
        c.setObservaciones(req.observaciones);
        c.setIdUsuarioEmisor(req.idUsuarioEmisor);

        return certDao.insertar(c);
    }

    /**
     * Emite un certificado de CAPACITACIÓN (modelo institucional simple).
     */
    public Certificado emitirCapacitacion(EmisionRequest req) {
        validar(req);
        if (req.notaFinal == null)
            throw new IllegalArgumentException("Nota final requerida");
        if (req.duracionHoras == null)
            throw new IllegalArgumentException("Duración en horas requerida");

        Alumno alumno = alumnoDao.buscarPorId(req.idAlumno)
                .orElseThrow(() -> new IllegalArgumentException("Alumno no existe"));
        Modulo modulo = moduloDao.buscarPorId(req.idModulo)
                .orElseThrow(() -> new IllegalArgumentException("Módulo no existe"));

        Integer idMat = matriculaDao.crear(req.idAlumno, req.idModulo,
                req.fechaInicioCurso != null ? req.fechaInicioCurso : req.fechaEmision,
                req.fechaFinCurso != null ? req.fechaFinCurso : req.fechaEmision,
                req.cicloFormativo);

        int anio = req.fechaEmision.getYear();
        int corr = certDao.siguienteCorrelativo(Certificado.Tipo.CAPACITACION, anio);
        String codigo = GeneradorCodigos.generar(Certificado.Tipo.CAPACITACION,
                corr, anio, modulo.getCodigoModulo(), alumno.getDni());

        Certificado c = new Certificado();
        c.setIdMatricula(idMat);
        c.setTipoCertificado(Certificado.Tipo.CAPACITACION);
        c.setNumeroCorrelativo(String.format("%05d", corr));
        c.setCodigoInstitucional(codigo);
        c.setAnioEmision(anio);
        c.setFechaEmision(req.fechaEmision);
        c.setLugarEmision(req.lugarEmision != null ? req.lugarEmision : "Ica");
        c.setNotaFinal(req.notaFinal);
        c.setNotaFinalTexto(NumeroATexto.convertir(req.notaFinal));
        c.setDuracionHoras(req.duracionHoras);
        c.setObservaciones(req.observaciones);
        c.setIdUsuarioEmisor(req.idUsuarioEmisor);

        return certDao.insertar(c);
    }

    public List<Certificado> listar() { return certDao.listarCompleto(); }
    public List<Certificado> listarPorAlumno(Integer id) { return certDao.listarPorAlumno(id); }
    public Optional<Certificado> buscar(Integer id) { return certDao.buscarPorId(id); }

    public boolean marcarImpreso(Integer id, boolean v) {
        return certDao.marcarImpreso(id, v);
    }
    public boolean marcarEntregado(Integer id, boolean v, String firma) {
        if (v && (firma == null || firma.isBlank()))
            throw new IllegalArgumentException("Firma del receptor requerida");
        return certDao.marcarEntregado(id, v, firma);
    }
    public boolean anular(Integer id) { return certDao.eliminar(id); }

    public List<Certificado> filtrar(String tipo, Integer anio, Boolean entregado) {
        Certificado.Tipo t = (tipo == null || tipo.isBlank()) ? null
                : Certificado.Tipo.valueOf(tipo);
        return certDao.filtrar(t, anio, entregado);
    }

    private void validar(EmisionRequest req) {
        if (req == null) throw new IllegalArgumentException("Request nulo");
        if (req.idAlumno == null) throw new IllegalArgumentException("Alumno requerido");
        if (req.idModulo == null) throw new IllegalArgumentException("Módulo requerido");
        if (req.fechaEmision == null) throw new IllegalArgumentException("Fecha requerida");
    }

    /** DTO de entrada para emisión. */
    public static class EmisionRequest {
        public Integer idAlumno;
        public Integer idModulo;
        public LocalDate fechaEmision;
        public LocalDate fechaInicioCurso;
        public LocalDate fechaFinCurso;
        public String cicloFormativo;
        public String lugarEmision;
        public Integer notaFinal;          // capacitación
        public Integer duracionHoras;      // capacitación
        public List<UnidadDidactica> notasPorUnidad; // modular
        public String observaciones;
        public Integer idUsuarioEmisor;
    }
}
