package com.unicolombo.bienestar.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginEstudianteRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "^UC\\d{6}$", message = "Formato: UC######")
    private String codigoEstudiantil;
}