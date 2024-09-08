package es.uah.huertojpa.maceta.dominio;

public class Humedad {
    Integer idMaceta;
    Float humedadMediaMinimaRequerida,humedadMediaMaximaRequerida;

    Integer numeroPlantas;

    public Humedad() {
        this.humedadMediaMinimaRequerida=0.0F;
        this.humedadMediaMaximaRequerida=0.0F;

        this.numeroPlantas=0;
    }



    public Integer getIdMaceta() {
        return idMaceta;
    }

    public void setIdMaceta(Integer idMaceta) {
        this.idMaceta = idMaceta;
    }

    public Float getHumedadMediaMinimaRequerida() {
        return humedadMediaMinimaRequerida;
    }

    public void setHumedadMediaMinimaRequerida(Float humedadMediaMinimaRequerida) {
        this.humedadMediaMinimaRequerida = humedadMediaMinimaRequerida;
    }

    public Float getHumedadMediaMaximaRequerida() {
        return humedadMediaMaximaRequerida;
    }

    public void setHumedadMediaMaximaRequerida(Float humedadMediaMaximaRequerida) {
        this.humedadMediaMaximaRequerida = humedadMediaMaximaRequerida;
    }

    public Integer getNumeroPlantas() {
        return numeroPlantas;
    }

    public void setNumeroPlantas(Integer numeroPlantas) {
        this.numeroPlantas = numeroPlantas;
    }

    public void addHumedadReqPorPlanta(Float MedidaMinima, Float MedidaMaxima){
        numeroPlantas=numeroPlantas+1;
        humedadMediaMinimaRequerida=(humedadMediaMinimaRequerida +MedidaMinima)/numeroPlantas;
        humedadMediaMaximaRequerida=(humedadMediaMaximaRequerida +MedidaMaxima)/numeroPlantas;

    }
}
