package com.blyp.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ListaDto {

    private UUID id;
    private String nombre;
    private List<ItemListaDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ListaDto(UUID id, String nombre, List<ItemListaDto> items, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id        = id;
        this.nombre    = nombre;
        this.items     = items;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId()                  { return id; }
    public String getNombre()            { return nombre; }
    public List<ItemListaDto> getItems() { return items; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
    public LocalDateTime getUpdatedAt()  { return updatedAt; }
}
