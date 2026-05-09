package com.blyp.dto;

import java.time.LocalDate;
import java.util.UUID;

public record MensajeDto(UUID id, String texto, String tipo, LocalDate fecha, boolean leido) {}
