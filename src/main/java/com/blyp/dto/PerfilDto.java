package com.blyp.dto;

import java.time.LocalDateTime;

public record PerfilDto(String nombre, String email, LocalDateTime joinDate, boolean isPro, boolean has2fa) {}
