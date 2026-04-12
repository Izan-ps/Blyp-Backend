package com.blyp.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "item_lista")
public class ItemLista {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lista_id", nullable = false)
    private Lista lista;

    @Column(nullable = false, length = 255)
    private String nombre;

    @Column(nullable = false)
    private int cantidad = 1;

    @Column(nullable = false)
    private boolean completado = false;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Lista getLista() { return lista; }
    public void setLista(Lista lista) { this.lista = lista; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public boolean isCompletado() { return completado; }
    public void setCompletado(boolean completado) { this.completado = completado; }
}
