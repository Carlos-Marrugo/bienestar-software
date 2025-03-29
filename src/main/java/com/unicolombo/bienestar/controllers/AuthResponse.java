package com.unicolombo.bienestar.controllers;

public class AuthResponse {
    private String token;
    private String message;
    private String email;
    private String codigoEstudiantil;
    private String rol;

    public AuthResponse(String token, String message, String email, String codigoEstudiantil, String rol) {
        this.token = token;
        this.message = message;
        this.email = email;
        this.codigoEstudiantil = codigoEstudiantil;
        this.rol = rol;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCodigoEstudiantil() {
        return codigoEstudiantil;
    }

    public void setCodigoEstudiantil(String codigoEstudiantil) {
        this.codigoEstudiantil = codigoEstudiantil;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}