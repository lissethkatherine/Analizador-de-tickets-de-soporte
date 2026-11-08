package com.ejemplo.tickets.repository;

import com.ejemplo.tickets.model.AnalisisTicket;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalisisTicketRepository extends JpaRepository<AnalisisTicket, Long> {

    /** Spring genera la consulta solo a partir del nombre del metodo. */
    List<AnalisisTicket> findTop20ByOrderByCreadoEnDesc();
}
