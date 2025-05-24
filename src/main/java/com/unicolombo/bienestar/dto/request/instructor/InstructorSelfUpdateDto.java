package com.unicolombo.bienestar.dto.request.instructor;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class InstructorSelfUpdateDto {
    @NotBlank(message = "Especialidad es obligatoria")
    private String especialidad;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;
}