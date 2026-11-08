package com.ejemplo.tickets.exception;

/** Falla de un servicio externo (OpenRouter, Qdrant) o respuesta invalida del LLM. Se responde con 502. */
public class ServicioExternoException extends RuntimeException {

    public ServicioExternoException(String mensaje) {
        super(mensaje);
    }

    public ServicioExternoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
