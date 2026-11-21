package es.uah.huertojpa.login.dominio;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {
    String userName, idUser,permisoAdmin ,resultado ,token;
    /** S8: la contraseña la puso el admin; el front lleva a «Cambiar contraseña» antes de dejar seguir. */
    boolean debeCambiarContrasena;
    /** S8b: solo si la contraseña era la temporal del admin y ya ha caducado (no se entra; hay que pedir otra). */
    Boolean temporalCaducada;
    /** S6: solo cuando el login está bloqueado por demasiados fallos (respuesta 429): segundos que faltan. */
    Long reintentarEnSegundos;

    public LoginResponse(String userName, String idUser, String permisoAdmin, String resultado, String token) {
        this.userName = userName;
        this.idUser = idUser;
        this.permisoAdmin = permisoAdmin;
        this.resultado = resultado;
        this.token = token;
    }

    public LoginResponse() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getPermisoAdmin() {
        return permisoAdmin;
    }

    public void setPermisoAdmin(String permisoAdmin) {
        this.permisoAdmin = permisoAdmin;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isDebeCambiarContrasena() {
        return debeCambiarContrasena;
    }

    public void setDebeCambiarContrasena(boolean debeCambiarContrasena) {
        this.debeCambiarContrasena = debeCambiarContrasena;
    }

    public Boolean getTemporalCaducada() {
        return temporalCaducada;
    }

    public void setTemporalCaducada(Boolean temporalCaducada) {
        this.temporalCaducada = temporalCaducada;
    }

    public Long getReintentarEnSegundos() {
        return reintentarEnSegundos;
    }

    public void setReintentarEnSegundos(Long reintentarEnSegundos) {
        this.reintentarEnSegundos = reintentarEnSegundos;
    }

    /** S6: ¿hay que responder 429? */
    public boolean bloqueado() {
        return reintentarEnSegundos != null;
    }
}
