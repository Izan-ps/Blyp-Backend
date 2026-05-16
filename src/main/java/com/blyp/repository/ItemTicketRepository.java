package com.blyp.repository;

import com.blyp.model.ItemTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ItemTicketRepository extends JpaRepository<ItemTicket, UUID> {
    List<ItemTicket> findByTicketId(UUID ticketId);

    @Query("SELECT COALESCE(SUM(i.cantidad * i.precioUnidad), 0) FROM ItemTicket i WHERE i.ticket.id = :ticketId")
    BigDecimal calcularTotalTicket(UUID ticketId);

    @Query("SELECT i.categoria, SUM(i.cantidad * i.precioUnidad) FROM ItemTicket i " +
           "WHERE i.ticket.usuario.id = :usuarioId AND i.ticket.fecha BETWEEN :desde AND :hasta " +
           "GROUP BY i.categoria ORDER BY SUM(i.cantidad * i.precioUnidad) DESC")
    List<Object[]> gastosPorCategoria(UUID usuarioId, LocalDate desde, LocalDate hasta);

    @Query("SELECT COALESCE(SUM(i.cantidad * i.precioUnidad), 0) FROM ItemTicket i " +
           "WHERE i.ticket.usuario.id = :usuarioId AND i.ticket.fecha BETWEEN :desde AND :hasta")
    BigDecimal totalGastos(UUID usuarioId, LocalDate desde, LocalDate hasta);
}
