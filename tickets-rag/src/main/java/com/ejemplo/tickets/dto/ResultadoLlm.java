package com.ejemplo.tickets.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Prediccion que devuelve el LLM (y que la API entrega al frontend).
 * Los nombres JSON van en snake_case para coincidir con lo que pide el prompt.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ResultadoLlm(
        String categoria,
        String prioridad,
        @JsonProperty("area_sugerida") String areaSugerida,
        @JsonProperty("tiempo_estimado_horas") Integer tiempoEstimadoHoras,
        Double confianza,
        String justificacion,
        @JsonProperty("casos_usados") List<Long> casosUsados) { }
