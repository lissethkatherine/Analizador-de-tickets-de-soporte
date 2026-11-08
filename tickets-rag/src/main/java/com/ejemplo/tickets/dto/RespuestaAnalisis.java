package com.ejemplo.tickets.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Respuesta completa de POST /analizar.
 * Si no hay casos suficientemente parecidos, "analisis" viene null
 * y "mensaje" explica por que.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RespuestaAnalisis(
        String texto,
        ResultadoLlm analisis,
        List<CasoSimilar> similares,
        String modelo,
        String mensaje) { }
