package com.blyp.controller;

import com.blyp.dto.TicketAnalisisRequest;
import com.blyp.service.TicketAnalisisService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketAnalisisService ticketAnalisisService;

    public TicketController(TicketAnalisisService ticketAnalisisService) {
        this.ticketAnalisisService = ticketAnalisisService;
    }

    @PostMapping("/analizar")
    public ResponseEntity<List<Map<String, Object>>> analizar(
            @Valid @RequestBody TicketAnalisisRequest request) {
        return ResponseEntity.ok(ticketAnalisisService.analizarTicket(request.getImagenBase64()));
    }
}
