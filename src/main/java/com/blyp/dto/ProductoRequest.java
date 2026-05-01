package com.blyp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProductoRequest {

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 255, message = "El nombre no puede superar los 255 caracteres")
    private String nombre;

    @Min(value = 0, message = "La cantidad no puede ser negativa")
    private int cantidad = 1;

    @Size(max = 100, message = "La categoría no puede superar los 100 caracteres")
    private String categoria;

    @Size(max = 50)
    private String codigoBarras;

    public String getNombre()    { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getCantidad()     { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }
}
