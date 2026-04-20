package pe.edu.cetpro.andina.entity;

import java.util.ArrayList;
import java.util.List;

public class Modulo {

    private Integer idModulo;
    private Integer idPrograma;
    private String nombrePrograma;         // JOIN con programas_estudio
    private String codigoModulo;           // CM1, CM2, ...
    private String nombre;
    private Integer totalCreditos;
    private Integer totalHoras;
    private String cicloFormativo;
    private String modalidad = "PRESENCIAL";
    private boolean activo = true;

    // Relación: unidades didácticas del módulo
    private List<UnidadDidactica> unidades = new ArrayList<>();

    public Modulo() {}

    // ====== Getters & Setters ======
    public Integer getIdModulo() { return idModulo; }
    public void setIdModulo(Integer idModulo) { this.idModulo = idModulo; }

    public Integer getIdPrograma() { return idPrograma; }
    public void setIdPrograma(Integer idPrograma) { this.idPrograma = idPrograma; }

    public String getNombrePrograma() { return nombrePrograma; }
    public void setNombrePrograma(String nombrePrograma) { this.nombrePrograma = nombrePrograma; }

    public String getCodigoModulo() { return codigoModulo; }
    public void setCodigoModulo(String codigoModulo) { this.codigoModulo = codigoModulo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getTotalCreditos() { return totalCreditos; }
    public void setTotalCreditos(Integer totalCreditos) { this.totalCreditos = totalCreditos; }

    public Integer getTotalHoras() { return totalHoras; }
    public void setTotalHoras(Integer totalHoras) { this.totalHoras = totalHoras; }

    public String getCicloFormativo() { return cicloFormativo; }
    public void setCicloFormativo(String cicloFormativo) { this.cicloFormativo = cicloFormativo; }

    public String getModalidad() { return modalidad; }
    public void setModalidad(String modalidad) { this.modalidad = modalidad; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public List<UnidadDidactica> getUnidades() { return unidades; }
    public void setUnidades(List<UnidadDidactica> unidades) { this.unidades = unidades; }
}
