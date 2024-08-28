package es.uah.huertojpa.login.dominio;

public class LoginResponse {
    String userName, idUser,permisoAdmin ,resultado ,token;

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
}
