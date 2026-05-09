package com.blyp.dto;

import jakarta.validation.constraints.NotBlank;

public class TicketAnalisisRequest {

    @NotBlank
    private String imagenBase64;

    public String getImagenBase64() { return imagenBase64; }
    public void setImagenBase64(String imagenBase64) { this.imagenBase64 = imagenBase64; }
}
