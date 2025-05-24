package com.unicolombo.bienestar.dto.request.estudiante;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginEstudianteRequest {
    @NotBlank @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "^UC\\d{6}$", message = "El código estudiantil debe tener formato UC######")
    private String codigoEstudiantil;
}