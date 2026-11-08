package com.ejemplo.tickets.service;

import com.ejemplo.tickets.dto.CasoSimilar;
import com.ejemplo.tickets.dto.RespuestaAnalisis;
import com.ejemplo.tickets.dto.ResultadoLlm;
import com.ejemplo.tickets.exception.ServicioExternoException;
import com.ejemplo.tickets.model.AnalisisTicket;
import com.ejemplo.tickets.repository.AnalisisTicketRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Flujo de un analisis en Spring Boot:
 *   1) envia el ticket a n8n (que hace embedding + Qdrant + LLM),
 *   2) valida lo que n8n devuelve,
 *   3) guarda el resultado en el historial (PostgreSQL),
 *   4) responde al frontend.
 */
@Service
public class AnalisisService {

    private static final String SIN_INFORMACION =
            "No hay información suficiente en el catálogo para predecir este ticket.";

    private final N8nService n8nService;
    private final AnalisisTicketRepository repositorio;

    public AnalisisService(N8nService n8nService, AnalisisTicketRepository repositorio) {
        this.n8nService = n8nService;
        this.repositorio = repositorio;
    }

    public RespuestaAnalisis analizar(String texto) {
        RespuestaAnalisis respuesta = n8nService.analizar(texto);
        List<CasoSimilar> similares = respuesta.similares() == null ? List.of() : respuesta.similares();

        // n8n no encontro casos parecidos: no hay prediccion que validar ni guardar.
        if (respuesta.analisis() == null) {
            String mensaje = (respuesta.mensaje() == null || respuesta.mensaje().isBlank())
                    ? SIN_INFORMACION : respuesta.mensaje();
            return new RespuestaAnalisis(texto, null, similares, respuesta.modelo(), mensaje);
        }

        ResultadoLlm validado = validar(respuesta.analisis(), similares);
        guardar(texto, validado, respuesta.modelo());
        return new RespuestaAnalisis(texto, validado, similares, respuesta.modelo(), null);
    }

    /** Segunda barrera de seguridad: aunque n8n ya valida, el backend vuelve a comprobar todo. */
    private ResultadoLlm validar(ResultadoLlm r, List<CasoSimilar> similares) {
        String categoria = Catalogo.canonico(r.categoria(), Catalogo.CATEGORIAS);
        String prioridad = Catalogo.canonico(r.prioridad(), Catalogo.PRIORIDADES);
        String area = Catalogo.canonico(r.areaSugerida(), Catalogo.AREAS);

        if (categoria == null || prioridad == null || area == null) {
            throw new ServicioExternoException(
                    "n8n devolvió valores fuera del catálogo permitido. Intenta de nuevo.");
        }

        int horas = (r.tiempoEstimadoHoras() != null && r.tiempoEstimadoHoras() >= 0)
                ? r.tiempoEstimadoHoras()
                : (int) Math.round(similares.stream()
                        .mapToInt(c -> c.horasResolucion() == null ? 0 : c.horasResolucion())
                        .average().orElse(24));

        double confianza = r.confianza() == null ? 0.0 : Math.max(0.0, Math.min(1.0, r.confianza()));

        Set<Long> idsValidos = similares.stream().map(CasoSimilar::id).collect(Collectors.toSet());
        List<Long> casosUsados = r.casosUsados() == null ? List.of()
                : r.casosUsados().stream().filter(idsValidos::contains).distinct().toList();

        String justificacion = r.justificacion() == null ? "" : r.justificacion().strip();

        return new ResultadoLlm(categoria, prioridad, area, horas, confianza, justificacion, casosUsados);
    }

    private void guardar(String texto, ResultadoLlm a, String modelo) {
        AnalisisTicket e = new AnalisisTicket();
        e.setTextoTicket(texto);
        e.setCategoria(a.categoria());
        e.setPrioridad(a.prioridad());
        e.setAreaSugerida(a.areaSugerida());
        e.setTiempoEstimadoHoras(a.tiempoEstimadoHoras());
        e.setConfianza(a.confianza());
        e.setJustificacion(a.justificacion());
        e.setIdsCasosUsados(a.casosUsados().stream().map(String::valueOf).collect(Collectors.joining(",")));
        e.setModelo(modelo);
        repositorio.save(e);
    }
}
