package com.ejemplo.tickets.controller;

import com.ejemplo.tickets.repository.AnalisisTicketRepository;
import com.ejemplo.tickets.service.N8nService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SaludController {

    private final AnalisisTicketRepository repositorio;
    private final N8nService n8n;

    public SaludController(AnalisisTicketRepository repositorio, N8nService n8n) {
        this.repositorio = repositorio;
        this.n8n = n8n;
    }

    /** Comprueba que la API, la base de datos y n8n respondan. */
    @GetMapping("/salud")
    public Map<String, String> salud() {
        Map<String, String> estado = new LinkedHashMap<>();
        estado.put("api", "ok");
        try {
            repositorio.count();
            estado.put("baseDatos", "ok");
        } catch (Exception e) {
            estado.put("baseDatos", "error");
        }
        estado.put("n8n", n8n.disponible() ? "ok" : "error");
        return estado;
    }
}
