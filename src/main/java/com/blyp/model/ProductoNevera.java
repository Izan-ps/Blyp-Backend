package com.blyp.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "producto_nevera")
public class ProductoNevera {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nevera_id", nullable = false)
    private Nevera nevera;

    @Column(nullable = false, length = 255)
    private String nombre;

    @Column(nullable = false)
    private int cantidad = 1;

    @Column(length = 100)
    private String categoria;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Nevera getNevera() { return nevera; }
    public void setNevera(Nevera nevera) { this.nevera = nevera; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
}
