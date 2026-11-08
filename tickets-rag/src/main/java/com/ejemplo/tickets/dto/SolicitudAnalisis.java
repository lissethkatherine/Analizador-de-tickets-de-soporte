package com.ejemplo.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cuerpo de POST /analizar: { "texto": "..." } */
public record SolicitudAnalisis(
        @NotBlank(message = "Escribe el ticket que quieres analizar.")
        @Size(min = 10, max = 2000, message = "El ticket debe tener entre 10 y 2000 caracteres.")
        String texto) { }
