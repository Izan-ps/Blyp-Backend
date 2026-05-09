package com.blyp.controller;

import com.blyp.dto.MensajeDto;
import com.blyp.service.MensajeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mensajes")
public class MensajeController {

    private final MensajeService mensajeService;

    public MensajeController(MensajeService mensajeService) {
        this.mensajeService = mensajeService;
    }

    @GetMapping
    public ResponseEntity<List<MensajeDto>> listar(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(mensajeService.listar(user.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID id) {
        mensajeService.eliminar(user.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
