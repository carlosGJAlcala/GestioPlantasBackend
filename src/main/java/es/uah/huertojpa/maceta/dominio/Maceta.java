package es.uah.huertojpa.maceta.dominio;

import es.uah.huertojpa.huerto.dominio.entidades.Huerto;
import jakarta.persistence.*;

@Entity
@Table(name = "maceta")
public class Maceta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdMaceta", nullable = false)
    private Integer id;

/*    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Huerto_idHuerto", nullable = false)
    private Huerto huertoIdhuerto;*/

    @Column(name = "PosicionMacetaenElHuerto", length = 45)
    private String posicionMacetaenElHuerto;

    @Basic
    @Column(name = "Huerto_idHuerto")
    private Integer huertoIdHuerto;

    public Integer getHuertoIdhuerto() {
        return huertoIdHuerto;
    }

    public void setHuertoIdhuerto(Integer huertoIdHuerto) {
        this.huertoIdHuerto = huertoIdHuerto;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

/*    public Huerto getHuertoIdhuerto() {
        return huertoIdhuerto;
    }*/

/*
    public void setHuertoIdhuerto(Huerto huertoIdhuerto) {
        this.huertoIdhuerto = huertoIdhuerto;
    }
*/

    public String getPosicionMacetaenElHuerto() {
        return posicionMacetaenElHuerto;
    }

    public void setPosicionMacetaenElHuerto(String posicionMacetaenElHuerto) {
        this.posicionMacetaenElHuerto = posicionMacetaenElHuerto;
    }

}