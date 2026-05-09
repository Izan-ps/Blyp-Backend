package com.blyp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ItemRequest {

    @NotBlank(message = "El nombre del ítem no puede estar vacío")
    @Size(max = 255)
    private String nombre;

    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private int cantidad = 1;

    public String getNombre()              { return nombre; }
    public void setNombre(String nombre)   { this.nombre = nombre; }

    public int getCantidad()               { return cantidad; }
    public void setCantidad(int cantidad)  { this.cantidad = cantidad; }
}
