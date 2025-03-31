package com.unicolombo.bienestar.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegistroEstudianteDto {

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El código estudiantil es obligatorio")
    @Pattern(regexp = "^UC\\d{6}$", message = "El código debe tener formato UC seguido de 6 dígitos")
    private String codigoEstudiantil;
}