package com.unicolombo.bienestar.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegistroEstudianteDto {
    @NotBlank @Email
    private String email;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
            message = "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula y un número")
    private String password;
    private String nombre;
    private String apellido;

    @Pattern(regexp = "^UC\\d{6}$", message = "El código estudiantil debe tener formato UC######")
    private String codigoEstudiantil;
}