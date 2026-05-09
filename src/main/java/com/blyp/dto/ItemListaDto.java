package com.blyp.dto;

import java.util.UUID;

public class ItemListaDto {

    private UUID id;
    private String nombre;
    private int cantidad;

    public ItemListaDto(UUID id, String nombre, int cantidad) {
        this.id       = id;
        this.nombre   = nombre;
        this.cantidad = cantidad;
    }

    public UUID getId()       { return id; }
    public String getNombre() { return nombre; }
    public int getCantidad()  { return cantidad; }
}
