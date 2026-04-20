package pe.edu.cetpro.andina.entity;

public class UnidadDidactica {

    private Integer idUnidad;
    private Integer idModulo;
    private String nombre;
    private Integer creditos;
    private Integer horas;
    private String capacidades;
    private String unidadCompetencia;
    private Integer orden;
    private boolean activo = true;

    // Nota asociada (opcional, se llena cuando es parte de una matrícula)
    private Integer calificacion;

    public UnidadDidactica() {}

    public Integer getIdUnidad() { return idUnidad; }
    public void setIdUnidad(Integer idUnidad) { this.idUnidad = idUnidad; }

    public Integer getIdModulo() { return idModulo; }
    public void setIdModulo(Integer idModulo) { this.idModulo = idModulo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getCreditos() { return creditos; }
    public void setCreditos(Integer creditos) { this.creditos = creditos; }

    public Integer getHoras() { return horas; }
    public void setHoras(Integer horas) { this.horas = horas; }

    public String getCapacidades() { return capacidades; }
    public void setCapacidades(String capacidades) { this.capacidades = capacidades; }

    public String getUnidadCompetencia() { return unidadCompetencia; }
    public void setUnidadCompetencia(String unidadCompetencia) { this.unidadCompetencia = unidadCompetencia; }

    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Integer getCalificacion() { return calificacion; }
    public void setCalificacion(Integer calificacion) { this.calificacion = calificacion; }
}
