package com.blyp.dto;

public class AuthResponse {

    private String token;
    private String nombre;
    private String email;
    private boolean isPro;

    public AuthResponse(String token, String nombre, String email, boolean isPro) {
        this.token = token;
        this.nombre = nombre;
        this.email = email;
        this.isPro = isPro;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isPro() { return isPro; }
    public void setPro(boolean isPro) { this.isPro = isPro; }
}
