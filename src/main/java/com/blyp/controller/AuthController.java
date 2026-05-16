package com.blyp.controller;

import com.blyp.dto.*;
import com.blyp.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensaje", "Revisa tu correo para verificar tu cuenta"));
    }

    @PostMapping("/verificar")
    public ResponseEntity<AuthResponse> verificar(@RequestBody VerificarRequest request) {
        return ResponseEntity.ok(authService.verificarEmail(request.email(), request.codigo()));
    }

    @PostMapping("/reenviar")
    public ResponseEntity<Map<String, String>> reenviar(@RequestBody ReenviarRequest request) {
        authService.reenviarVerificacion(request.email());
        return ResponseEntity.ok(Map.of("mensaje", "Código reenviado"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/verificar-2fa")
    public ResponseEntity<AuthResponse> verificar2fa(@RequestBody VerificarRequest request) {
        return ResponseEntity.ok(authService.verificar2fa(request.email(), request.codigo()));
    }
}
