package com.blyp.controller;

import com.blyp.dto.ListaDto;
import com.blyp.dto.ListaRequest;
import com.blyp.service.ListaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/listas")
public class ListaController {

    private final ListaService listaService;

    public ListaController(ListaService listaService) {
        this.listaService = listaService;
    }

    @GetMapping
    public ResponseEntity<List<ListaDto>> listar(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(listaService.listar(user.getUsername()));
    }

    @PostMapping
    public ResponseEntity<ListaDto> crear(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody ListaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(listaService.crear(user.getUsername(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ListaDto> editar(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID id,
            @Valid @RequestBody ListaRequest request) {
        return ResponseEntity.ok(listaService.editar(user.getUsername(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID id) {
        listaService.eliminar(user.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/auto")
    public ResponseEntity<?> listaAuto(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(listaService.listaAuto(user.getUsername()));
    }
}
