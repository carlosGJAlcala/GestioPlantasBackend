package es.uah.huertojpa.registro.dominio.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "fechahora")
public class Fechahora {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idFecha", nullable = false)
    private Integer id;

    @Column(name = "anyio", length = 45)
    private String anyio;

    @Column(name = "mes", length = 45)
    private String mes;

    @Column(name = "dia", length = 45)
    private String dia;

    @Column(name = "hora", length = 45)
    private String hora;

    @Column(name = "minuto", length = 45)
    private String minuto;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAnyio() {
        return anyio;
    }

    public void setAnyio(String anyio) {
        this.anyio = anyio;
    }

    public String getMes() {
        return mes;
    }

    public void setMes(String mes) {
        this.mes = mes;
    }

    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getMinuto() {
        return minuto;
    }

    public void setMinuto(String minuto) {
        this.minuto = minuto;
    }

}