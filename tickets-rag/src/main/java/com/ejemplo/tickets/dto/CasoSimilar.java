package com.ejemplo.tickets.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Un ticket historico recuperado de Qdrant, con su porcentaje de similitud. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CasoSimilar(
        Long id,
        String texto,
        String categoria,
        String prioridad,
        String area,
        @JsonProperty("horas_resolucion") Integer horasResolucion,
        @JsonProperty("similitud_porcentaje") double similitudPorcentaje) { }
