package es.uah.huertojpa.persona.dominio.entidades;

public class LoginResp {
    String userName, idUser,permisoAdmin ,resultado;

    public LoginResp(String userName, String idUser, String permisoAdmin, String resultado) {
        this.userName = userName;
        this.idUser = idUser;
        this.permisoAdmin = permisoAdmin;
        this.resultado = resultado;
    }

    public LoginResp() {
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
}
