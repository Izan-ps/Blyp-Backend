package com.blyp.controller;

import com.blyp.config.PlanesConfig;
import com.blyp.dto.GuardarTicketRequest;
import com.blyp.dto.TicketAnalisisRequest;
import com.blyp.model.Usuario;
import com.blyp.repository.TicketRepository;
import com.blyp.repository.UsuarioRepository;
import com.blyp.service.GastosService;
import com.blyp.service.TicketAnalisisService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketAnalisisService ticketAnalisisService;
    private final GastosService gastosService;
    private final TicketRepository ticketRepository;
    private final UsuarioRepository usuarioRepository;
    private final PlanesConfig planesConfig;

    public TicketController(TicketAnalisisService ticketAnalisisService,
                            GastosService gastosService,
                            TicketRepository ticketRepository,
                            UsuarioRepository usuarioRepository,
                            PlanesConfig planesConfig) {
        this.ticketAnalisisService = ticketAnalisisService;
        this.gastosService         = gastosService;
        this.ticketRepository      = ticketRepository;
        this.usuarioRepository     = usuarioRepository;
        this.planesConfig          = planesConfig;
    }

    @PostMapping("/analizar")
    public ResponseEntity<Map<String, Object>> analizar(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TicketAnalisisRequest request) {

        String imagenBase64 = request.getImagenBase64();
        if (imagenBase64.length() > 10_000_000) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "La imagen es demasiado grande (máximo ~7 MB)"));
        }

        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        LocalDate desde = LocalDate.now().withDayOfMonth(1);
        LocalDate hasta = LocalDate.now();
        long usados = ticketRepository.countByUsuarioIdAndFechaBetween(usuario.getId(), desde, hasta);
        int limite  = planesConfig.getLimiteTickets(usuario.getRole().name());

        if (usados >= limite) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "Has alcanzado el límite de " + limite + " lecturas de ticket este mes."));
        }

        return ResponseEntity.ok(ticketAnalisisService.analizarTicket(request.getImagenBase64()));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> guardar(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody GuardarTicketRequest request) {
        return ResponseEntity.ok(gastosService.guardarTicket(user.getUsername(), request));
    }

    @GetMapping("/gastos")
    public ResponseEntity<Map<String, Object>> gastos(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(defaultValue = "mes") String periodo) {
        return ResponseEntity.ok(gastosService.getGastos(user.getUsername(), periodo));
    }
}
