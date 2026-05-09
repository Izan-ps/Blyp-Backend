package com.blyp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ListaRequest {

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 255)
    private String nombre;

    @NotNull
    @Valid
    private List<ItemRequest> items;

    public String getNombre()               { return nombre; }
    public void setNombre(String nombre)    { this.nombre = nombre; }

    public List<ItemRequest> getItems()              { return items; }
    public void setItems(List<ItemRequest> items)    { this.items = items; }
}
