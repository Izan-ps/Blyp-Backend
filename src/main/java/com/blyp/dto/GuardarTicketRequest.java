package com.blyp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class GuardarTicketRequest {

    private LocalDate fecha;
    private String tienda;
    private List<Item> items;

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getTienda() { return tienda; }
    public void setTienda(String tienda) { this.tienda = tienda; }

    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }

    public static class Item {
        private String nombre;
        private int cantidad;
        private BigDecimal precioUnidad;
        private String categoria;

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }

        public BigDecimal getPrecioUnidad() { return precioUnidad; }
        public void setPrecioUnidad(BigDecimal precioUnidad) { this.precioUnidad = precioUnidad; }

        public String getCategoria() { return categoria; }
        public void setCategoria(String categoria) { this.categoria = categoria; }
    }
}
