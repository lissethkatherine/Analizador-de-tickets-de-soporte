package com.ejemplo.tickets.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Historial: un registro por cada ticket analizado por el sistema. */
@Entity
@Table(name = "analisis_ticket")
@Getter
@Setter
@NoArgsConstructor
public class AnalisisTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String textoTicket;

    private String categoria;
    private String prioridad;
    private String areaSugerida;
    private Integer tiempoEstimadoHoras;
    private Double confianza;

    @Column(length = 2000)
    private String justificacion;

    /** Ids de los casos historicos citados, separados por coma. Ej: "3,7,12". */
    private String idsCasosUsados;

    private String modelo;

    private LocalDateTime creadoEn;

    @PrePersist
    void antesDeGuardar() {
        this.creadoEn = LocalDateTime.now();
    }
}
