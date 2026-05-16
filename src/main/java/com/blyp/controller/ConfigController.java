package com.blyp.controller;

import com.blyp.config.PlanesConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final PlanesConfig planesConfig;

    public ConfigController(PlanesConfig planesConfig) {
        this.planesConfig = planesConfig;
    }

    @GetMapping("/planes")
    public ResponseEntity<Map<String, Object>> planes() {
        return ResponseEntity.ok(Map.of(
            "user", Map.of("ticketsMes", planesConfig.getUser().getTicketsMes()),
            "pro",  Map.of("ticketsMes", planesConfig.getPro().getTicketsMes())
        ));
    }
}
