package com.unicolombo.bienestar.controllers;

public class AuthResponse {
    private String token;
    private String message;
    private String usuario;


    public AuthResponse(String token, String message, String usuario) {
        this.token = token;
        this.message = message;
        this.usuario = usuario;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
}