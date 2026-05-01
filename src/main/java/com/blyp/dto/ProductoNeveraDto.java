package com.blyp.dto;

import java.util.UUID;

public class ProductoNeveraDto {

    private UUID id;
    private String nombre;
    private int cantidad;
    private String categoria;
    private String codigoBarras;

    public ProductoNeveraDto(UUID id, String nombre, int cantidad, String categoria, String codigoBarras) {
        this.id          = id;
        this.nombre      = nombre;
        this.cantidad    = cantidad;
        this.categoria   = categoria;
        this.codigoBarras = codigoBarras;
    }

    public UUID getId()             { return id; }
    public String getNombre()       { return nombre; }
    public int getCantidad()        { return cantidad; }
    public String getCategoria()    { return categoria; }
    public String getCodigoBarras() { return codigoBarras; }
}
