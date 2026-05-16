package com.blyp.controller;

import com.blyp.service.RecetasService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/recetas")
public class RecetasController {

    private final RecetasService recetasService;

    public RecetasController(RecetasService recetasService) {
        this.recetasService = recetasService;
    }

    @PostMapping("/generar")
    public ResponseEntity<Map<String, Object>> generar(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody Map<String, String> body) {
        String mensaje = body.getOrDefault("mensaje", "").trim()
                .replaceAll("[\\p{Cntrl}]", " ")  // elimina caracteres de control
                .trim();
        if (mensaje.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El mensaje no puede estar vacío"));
        }
        if (mensaje.length() > 300) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El mensaje es demasiado largo (máximo 300 caracteres)"));
        }
        Map<String, Object> receta = recetasService.generarReceta(user.getUsername(), mensaje);
        return ResponseEntity.ok(receta);
    }
}
