package com.blyp.repository;

import com.blyp.model.ItemTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ItemTicketRepository extends JpaRepository<ItemTicket, UUID> {
    List<ItemTicket> findByTicketId(UUID ticketId);

    @Query("SELECT COALESCE(SUM(i.cantidad * i.precioUnidad), 0) FROM ItemTicket i WHERE i.ticket.id = :ticketId")
    BigDecimal calcularTotalTicket(UUID ticketId);
}
