package com.unicolombo.bienestar.dto.request.instructor;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class InstructorAdminUpdateDto {
    @NotBlank(message = "Especialidad es obligatoria")
    private String especialidad;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;
}