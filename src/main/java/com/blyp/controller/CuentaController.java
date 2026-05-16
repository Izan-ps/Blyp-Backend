package com.blyp.controller;

import com.blyp.dto.CambiarPasswordRequest;
import com.blyp.dto.PerfilDto;
import com.blyp.service.CuentaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cuenta")
public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @GetMapping("/perfil")
    public ResponseEntity<PerfilDto> getPerfil(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(cuentaService.getPerfil(user.getUsername()));
    }

    @PostMapping("/solicitar-cambio-password")
    public ResponseEntity<Map<String, String>> solicitarCambio(@AuthenticationPrincipal UserDetails user) {
        cuentaService.solicitarCambioPassword(user.getUsername());
        return ResponseEntity.ok(Map.of("mensaje", "Código enviado a tu correo"));
    }

    @PostMapping("/cambiar-password")
    public ResponseEntity<Map<String, String>> cambiarPassword(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody CambiarPasswordRequest request) {
        cuentaService.cambiarPassword(user.getUsername(), request.codigo(), request.nuevaPassword());
        return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada"));
    }

    @PostMapping("/toggle-2fa")
    public ResponseEntity<Map<String, Object>> toggle2fa(@AuthenticationPrincipal UserDetails user) {
        boolean activo = cuentaService.toggle2fa(user.getUsername());
        return ResponseEntity.ok(Map.of("has2fa", activo));
    }

    @DeleteMapping
    public ResponseEntity<Void> eliminarCuenta(@AuthenticationPrincipal UserDetails user) {
        cuentaService.eliminarCuenta(user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
