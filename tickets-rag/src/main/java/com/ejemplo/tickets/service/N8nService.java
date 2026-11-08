package com.ejemplo.tickets.service;

import com.ejemplo.tickets.config.AppProperties;
import com.ejemplo.tickets.dto.RespuestaAnalisis;
import com.ejemplo.tickets.exception.ServicioExternoException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Conecta Spring Boot con la automatizacion de n8n.
 *
 * n8n hace todo el RAG: embedding del ticket, busqueda en Qdrant, armado del
 * contexto y consulta al LLM. Spring Boot solo le envia el texto y recibe el analisis.
 */
@Service
public class N8nService {

    private final RestClient cliente;
    private final URI webhook;
    private final ObjectMapper mapper;

    public N8nService(RestClient n8nClient, AppProperties props, ObjectMapper mapper) {
        this.cliente = n8nClient;
        this.webhook = URI.create(props.n8n().webhookUrl());
        this.mapper = mapper;
    }

    /** Envia el ticket al webhook de n8n y devuelve su respuesta ya convertida a objeto. */
    public RespuestaAnalisis analizar(String texto) {
        JsonNode respuesta;
        try {
            respuesta = cliente.post()
                    .uri(webhook)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("texto", texto))
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException e) {
            int codigo = e.getStatusCode().value();
            if (codigo == 404) {
                throw new ServicioExternoException(
                        "n8n respondió 404: el workflow no está activo o la URL del webhook es incorrecta "
                                + "(usa /webhook/, no /webhook-test/).", e);
            }
            throw new ServicioExternoException(
                    "n8n respondió con error " + codigo + ". Revisa la ejecución en n8n "
                            + "(credencial de OpenRouter, Qdrant y carga de tickets).", e);
        } catch (ResourceAccessException e) {
            throw new ServicioExternoException("No se pudo conectar con n8n. ¿Está encendido?", e);
        }

        // Respond to Webhook a veces entrega una lista con un solo elemento
        if (respuesta != null && respuesta.isArray() && !respuesta.isEmpty()) {
            respuesta = respuesta.get(0);
        }
        if (respuesta == null || !respuesta.isObject()) {
            throw new ServicioExternoException("n8n devolvió una respuesta vacía o inválida.");
        }

        try {
            return mapper.treeToValue(respuesta, RespuestaAnalisis.class);
        } catch (JsonProcessingException e) {
            throw new ServicioExternoException("n8n devolvió un formato inesperado.", e);
        }
    }

    /** Para /salud: n8n expone /healthz en la raiz de su servidor. */
    public boolean disponible() {
        try {
            cliente.get().uri(webhook.resolve("/healthz")).retrieve().toBodilessEntity();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
