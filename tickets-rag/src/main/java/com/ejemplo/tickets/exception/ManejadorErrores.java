package com.ejemplo.tickets.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Convierte las excepciones en respuestas JSON { "error": "..." } con el
 * codigo HTTP correcto, para que el frontend siempre reciba el mismo formato.
 */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    public record ErrorApi(String error) { }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorApi> validacion(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .findFirst().map(f -> f.getDefaultMessage()).orElse("Datos inválidos.");
        return ResponseEntity.badRequest().body(new ErrorApi(mensaje));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorApi> cuerpoInvalido(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(new ErrorApi("El cuerpo de la petición no es un JSON válido."));
    }

    @ExceptionHandler(ServicioExternoException.class)
    public ResponseEntity<ErrorApi> servicioExterno(ServicioExternoException e) {
        log.error("Fallo de servicio externo: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ErrorApi(e.getMessage()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorApi> estado(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(new ErrorApi(e.getReason()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorApi> inesperado(Exception e) {
        log.error("Error inesperado", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorApi("Error interno del servidor."));
    }
}
