package com.unicolombo.bienestar.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRequest {
    private String email;
    private String codigoEstudiantil;

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
}
