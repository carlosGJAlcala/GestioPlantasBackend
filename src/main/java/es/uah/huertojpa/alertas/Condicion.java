package es.uah.huertojpa.alertas;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Un problema detectado ahora mismo en una maceta, con el detalle legible para el usuario. */
public record Condicion(TipoAlerta tipo, NivelAlerta nivel, String mensaje) {
    @JsonProperty("titulo")
    public String titulo() {
        return tipo.getTitulo();
    }
}
