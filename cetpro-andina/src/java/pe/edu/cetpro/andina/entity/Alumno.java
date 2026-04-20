package pe.edu.cetpro.andina.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad Alumno - POJO puro.
 * Sigue SRP: solo guarda estado, sin lógica de BD.
 */
public class Alumno {

    private Integer idAlumno;
    private String dni;
    private String tipoDocumento;       // DNI | CE | PASAPORTE
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private LocalDate fechaNacimiento;
    private String genero;
    private String telefono;
    private String email;
    private String direccion;
    private boolean activo = true;
    private LocalDateTime fechaRegistro;

    // Campos calculados (desde vista v_alumnos_certificados)
    private Integer totalCertificados;
    private Integer certificadosEntregados;
    private Integer certificadosPendientes;

    public Alumno() {}

    /** Devuelve el nombre completo formateado para certificados. */
    public String getNombreCompleto() {
        StringBuilder sb = new StringBuilder();
        if (nombres != null) sb.append(nombres);
        if (apellidoPaterno != null) sb.append(" ").append(apellidoPaterno);
        if (apellidoMaterno != null) sb.append(" ").append(apellidoMaterno);
        return sb.toString().trim().toUpperCase();
    }

    // ====== Getters & Setters ======
    public Integer getIdAlumno() { return idAlumno; }
    public void setIdAlumno(Integer idAlumno) { this.idAlumno = idAlumno; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidoPaterno() { return apellidoPaterno; }
    public void setApellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; }

    public String getApellidoMaterno() { return apellidoMaterno; }
    public void setApellidoMaterno(String apellidoMaterno) { this.apellidoMaterno = apellidoMaterno; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public Integer getTotalCertificados() { return totalCertificados; }
    public void setTotalCertificados(Integer totalCertificados) { this.totalCertificados = totalCertificados; }

    public Integer getCertificadosEntregados() { return certificadosEntregados; }
    public void setCertificadosEntregados(Integer certificadosEntregados) { this.certificadosEntregados = certificadosEntregados; }

    public Integer getCertificadosPendientes() { return certificadosPendientes; }
    public void setCertificadosPendientes(Integer certificadosPendientes) { this.certificadosPendientes = certificadosPendientes; }
}
