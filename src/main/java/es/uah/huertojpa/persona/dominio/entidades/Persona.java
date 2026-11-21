package es.uah.huertojpa.persona.dominio.entidades;

import es.uah.huertojpa.login.dominio.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "persona")
public class Persona  implements UserDetails {
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

    // S2: guarda BCrypt (60 caracteres; la columna es VARCHAR(100)). Solo se lee del JSON (altas), nunca se
    // devuelve: antes GET /persona/... enviaba el hash, que además era justo lo que pedía el login.
    @Column(name = "passwordSHA256", length = 100)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String passwordSHA256;

    @Column(name = "DNI", length = 45)
    private String dni;

    /**
     * A3: recibir por correo las alertas de sus macetas (V7; por defecto sí). Sin valor inicial a propósito: un PUT
     * que no trae el campo llega con null y {@code actualizarPersona} conserva el que había; en las altas lo pone
     * {@link #porDefecto()}.
     */
    @Column(name = "avisos_correo", nullable = false)
    private Boolean avisosCorreo;

    /**
     * S8 (V8): la contraseña la ha puesto el administrador y hay que cambiarla al entrar. Solo lectura en el JSON:
     * la cambian el restablecimiento y el cambio de contraseña, nunca un {@code PUT /persona}.
     */
    @Column(name = "debe_cambiar_contrasena", nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean debeCambiarContrasena;

    /** S8 (V8): último cambio de contraseña. Los JWT emitidos antes ya no valen. Fuera del JSON. */
    @Column(name = "contrasena_cambiada")
    @JsonIgnore
    private LocalDateTime contrasenaCambiada;

    /** S9 (V9): «cerrar sesión en todos los equipos». Los JWT emitidos antes ya no valen. Fuera del JSON. */
    @Column(name = "sesiones_cerradas")
    @JsonIgnore
    private LocalDateTime sesionesCerradas;

    /** V8: último login correcto, para la lista de personas del admin. Solo lectura en el JSON. */
    @Column(name = "ultimo_acceso")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime ultimoAcceso;

    @PrePersist
    void porDefecto() {
        if (avisosCorreo == null) avisosCorreo = Boolean.TRUE;
        if (debeCambiarContrasena == null) debeCambiarContrasena = Boolean.FALSE;
    }

    public Boolean getDebeCambiarContrasena() {
        return debeCambiarContrasena;
    }

    public void setDebeCambiarContrasena(Boolean debeCambiarContrasena) {
        this.debeCambiarContrasena = debeCambiarContrasena;
    }

    @JsonIgnore
    public boolean tieneQueCambiarContrasena() {
        return Boolean.TRUE.equals(debeCambiarContrasena);
    }

    public LocalDateTime getContrasenaCambiada() {
        return contrasenaCambiada;
    }

    public void setContrasenaCambiada(LocalDateTime contrasenaCambiada) {
        this.contrasenaCambiada = contrasenaCambiada;
    }

    public LocalDateTime getSesionesCerradas() {
        return sesionesCerradas;
    }

    public void setSesionesCerradas(LocalDateTime sesionesCerradas) {
        this.sesionesCerradas = sesionesCerradas;
    }

    /**
     * S8/S9: momento a partir del cual valen los JWT de esta persona (el último cambio de contraseña o cierre de
     * todas las sesiones), o null si cualquiera vale.
     */
    @JsonIgnore
    public LocalDateTime tokensValidosDesde() {
        if (contrasenaCambiada == null) return sesionesCerradas;
        if (sesionesCerradas == null) return contrasenaCambiada;
        return contrasenaCambiada.isAfter(sesionesCerradas) ? contrasenaCambiada : sesionesCerradas;
    }

    public LocalDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }

    public void setUltimoAcceso(LocalDateTime ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }

    public Boolean getAvisosCorreo() {
        return avisosCorreo;
    }

    public void setAvisosCorreo(Boolean avisosCorreo) {
        this.avisosCorreo = avisosCorreo;
    }

    /** Para las alertas: solo un {@code false} explícito desactiva el correo. */
    @JsonIgnore
    public boolean quiereAvisosPorCorreo() {
        return !Boolean.FALSE.equals(avisosCorreo);
    }
    @Enumerated(EnumType.STRING)
    Role role;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

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

    // Campos de UserDetails: fuera del JSON (S8). Al devolverlos, un PUT con la ficha recibida daba 500.
    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // La columna role puede estar a NULL en filas antiguas: antes daba NullPointerException en cada petición.
        return List.of(new SimpleGrantedAuthority(role == null ? Role.USER.name() : role.name()));
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return passwordSHA256;
    }

    @JsonIgnore
    @Override
    public String getUsername() {
        return usuario;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return true;
    }
}