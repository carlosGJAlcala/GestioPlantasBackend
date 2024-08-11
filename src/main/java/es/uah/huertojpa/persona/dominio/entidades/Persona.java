package es.uah.huertojpa.persona.dominio.entidades;

import jakarta.persistence.*;

@Entity
@Table(name = "persona")
public class Persona {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Integer id;

    @Column(name = "Nombre", length = 45)
    private String nombre;

    @Column(name = "Apellido", length = 45)
    private String apellido;

    @Column(name = "Usuario", length = 45)
    private String usuario;

    @Column(name = "CorreoElectronico", length = 45)
    private String correoElectronico;

    @Column(name = "passwordSHA256", length = 45)
    private String passwordSHA256;

    @Column(name = "DNI", length = 45)
    private String dni;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public String getPasswordSHA256() {
        return passwordSHA256;
    }

    public void setPasswordSHA256(String passwordSHA256) {
        this.passwordSHA256 = passwordSHA256;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

}