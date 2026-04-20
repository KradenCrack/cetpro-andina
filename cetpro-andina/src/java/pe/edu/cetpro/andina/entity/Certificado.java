package pe.edu.cetpro.andina.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Certificado {

    public enum Tipo {
        CAPACITACION,       // Modelo institucional simple (nota única)
        MODULAR,            // Con tabla detallada de unidades didácticas
        AUXILIAR_TECNICO,   // Título tipo 0001AT-DNI-AÑO
        TECNICO             // Título tipo 0001T-DNI-AÑO
    }

    public enum Estado {
        EMITIDO, ANULADO, REEMPLAZADO
    }

    private Integer idCertificado;
    private Integer idMatricula;
    private Tipo tipoCertificado;

    private String numeroCorrelativo;        // "0026", "00320"
    private String codigoInstitucional;      // "0026-1CM1-2024"
    private String numeroRegistro;           // "N°___" que va en el modelo

    private Integer anioEmision;
    private LocalDate fechaEmision;
    private String lugarEmision = "Ica";

    // Solo para CAPACITACION
    private Integer notaFinal;
    private String notaFinalTexto;
    private Integer duracionHoras;

    // Solo para MODULAR: se calcula promedio
    private BigDecimal promedioFinal;

    // Control de estado (lo que pediste con checkboxes)
    private boolean impreso;
    private LocalDateTime fechaImpresion;
    private boolean entregado;
    private LocalDateTime fechaEntrega;
    private String firmaReceptor;

    private Estado estado = Estado.EMITIDO;
    private String observaciones;

    private Integer idUsuarioEmisor;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // Datos derivados (por JOINs) - útiles para el frontend
    private String alumnoNombre;
    private String alumnoDni;
    private String moduloNombre;
    private String moduloCodigo;
    private String programaNombre;
    private String cicloFormativo;
    private LocalDate fechaInicioCurso;
    private LocalDate fechaFinCurso;

    public Certificado() {}

    // ====== Getters & Setters ======
    public Integer getIdCertificado() { return idCertificado; }
    public void setIdCertificado(Integer idCertificado) { this.idCertificado = idCertificado; }

    public Integer getIdMatricula() { return idMatricula; }
    public void setIdMatricula(Integer idMatricula) { this.idMatricula = idMatricula; }

    public Tipo getTipoCertificado() { return tipoCertificado; }
    public void setTipoCertificado(Tipo tipoCertificado) { this.tipoCertificado = tipoCertificado; }

    public String getNumeroCorrelativo() { return numeroCorrelativo; }
    public void setNumeroCorrelativo(String numeroCorrelativo) { this.numeroCorrelativo = numeroCorrelativo; }

    public String getCodigoInstitucional() { return codigoInstitucional; }
    public void setCodigoInstitucional(String codigoInstitucional) { this.codigoInstitucional = codigoInstitucional; }

    public String getNumeroRegistro() { return numeroRegistro; }
    public void setNumeroRegistro(String numeroRegistro) { this.numeroRegistro = numeroRegistro; }

    public Integer getAnioEmision() { return anioEmision; }
    public void setAnioEmision(Integer anioEmision) { this.anioEmision = anioEmision; }

    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }

    public String getLugarEmision() { return lugarEmision; }
    public void setLugarEmision(String lugarEmision) { this.lugarEmision = lugarEmision; }

    public Integer getNotaFinal() { return notaFinal; }
    public void setNotaFinal(Integer notaFinal) { this.notaFinal = notaFinal; }

    public String getNotaFinalTexto() { return notaFinalTexto; }
    public void setNotaFinalTexto(String notaFinalTexto) { this.notaFinalTexto = notaFinalTexto; }

    public Integer getDuracionHoras() { return duracionHoras; }
    public void setDuracionHoras(Integer duracionHoras) { this.duracionHoras = duracionHoras; }

    public BigDecimal getPromedioFinal() { return promedioFinal; }
    public void setPromedioFinal(BigDecimal promedioFinal) { this.promedioFinal = promedioFinal; }

    public boolean isImpreso() { return impreso; }
    public void setImpreso(boolean impreso) { this.impreso = impreso; }

    public LocalDateTime getFechaImpresion() { return fechaImpresion; }
    public void setFechaImpresion(LocalDateTime fechaImpresion) { this.fechaImpresion = fechaImpresion; }

    public boolean isEntregado() { return entregado; }
    public void setEntregado(boolean entregado) { this.entregado = entregado; }

    public LocalDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDateTime fechaEntrega) { this.fechaEntrega = fechaEntrega; }

    public String getFirmaReceptor() { return firmaReceptor; }
    public void setFirmaReceptor(String firmaReceptor) { this.firmaReceptor = firmaReceptor; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public Integer getIdUsuarioEmisor() { return idUsuarioEmisor; }
    public void setIdUsuarioEmisor(Integer idUsuarioEmisor) { this.idUsuarioEmisor = idUsuarioEmisor; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public String getAlumnoNombre() { return alumnoNombre; }
    public void setAlumnoNombre(String alumnoNombre) { this.alumnoNombre = alumnoNombre; }

    public String getAlumnoDni() { return alumnoDni; }
    public void setAlumnoDni(String alumnoDni) { this.alumnoDni = alumnoDni; }

    public String getModuloNombre() { return moduloNombre; }
    public void setModuloNombre(String moduloNombre) { this.moduloNombre = moduloNombre; }

    public String getModuloCodigo() { return moduloCodigo; }
    public void setModuloCodigo(String moduloCodigo) { this.moduloCodigo = moduloCodigo; }

    public String getProgramaNombre() { return programaNombre; }
    public void setProgramaNombre(String programaNombre) { this.programaNombre = programaNombre; }

    public String getCicloFormativo() { return cicloFormativo; }
    public void setCicloFormativo(String cicloFormativo) { this.cicloFormativo = cicloFormativo; }

    public LocalDate getFechaInicioCurso() { return fechaInicioCurso; }
    public void setFechaInicioCurso(LocalDate fechaInicioCurso) { this.fechaInicioCurso = fechaInicioCurso; }

    public LocalDate getFechaFinCurso() { return fechaFinCurso; }
    public void setFechaFinCurso(LocalDate fechaFinCurso) { this.fechaFinCurso = fechaFinCurso; }
}
