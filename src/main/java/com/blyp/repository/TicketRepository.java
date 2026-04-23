package com.blyp.repository;

import com.blyp.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    List<Ticket> findByUsuarioIdOrderByFechaDesc(UUID usuarioId);
    List<Ticket> findByUsuarioIdAndFechaBetween(UUID usuarioId, LocalDate desde, LocalDate hasta);
}
