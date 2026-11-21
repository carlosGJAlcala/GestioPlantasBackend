package es.uah.huertojpa.sensores.dominio.entidades;

import java.util.List;

/**
 * W12b – Un sensor con lo que mide y dónde se usa, para la pantalla de tarjetas de sensores y el desplegable del
 * depósito. {@code macetas}: ids de las macetas que lo usan; {@code huertoDeposito}: huerto cuyo depósito lo usa (o
 * {@code null}); {@code libre}: no lo usa nada, se puede asociar a una maceta o a un depósito.
 */
public record SensorUso(Integer id, String nombreSensor, String magnitudAMedir, String cantidadMedida,
                        String unidades, List<Integer> macetas, Integer huertoDeposito, boolean libre) {
}
