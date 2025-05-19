package com.unicolombo.bienestar.controllers;

import com.unicolombo.bienestar.models.Usuario;

public class AuthResponse {
    private Long estudianteId;
    private Long instructorId;
    private String token;
    private String refreshToken;
    private Usuario usuario;

    public AuthResponse(Long estudianteId, String token, String refreshToken, Usuario usuario) {
        this.estudianteId = estudianteId;
        this.token = token;
        this.refreshToken = refreshToken;
        this.usuario = usuario;
    }

    public AuthResponse(String token, String refreshToken, Usuario usuario, Long instructorId) {
        this.instructorId = instructorId;
        this.token = token;
        this.refreshToken = refreshToken;
        this.usuario = usuario;
    }

    public Long getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(Long estudianteId) {
        this.estudianteId = estudianteId;
    }

    public Long getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(Long instructorId) {
        this.instructorId = instructorId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}