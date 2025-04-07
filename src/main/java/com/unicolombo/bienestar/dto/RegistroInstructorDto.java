package com.unicolombo.bienestar.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegistroInstructorDto {
    @NotBlank(message = "Email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String email;

    @NotBlank(message = "Contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "La contraseña debe contener mayúsculas, minúsculas y números")
    private String password;

    @NotBlank(message = "Nombre es obligatorio")
    @Size(max = 100, message = "Nombre no puede exceder 100 caracteres")
    private String nombre;

    @NotBlank(message = "Apellido es obligatorio")
    @Size(max = 100, message = "Apellido no puede exceder 100 caracteres")
    private String apellido;

    @NotBlank(message = "Especialidad es obligatoria")
    private String especialidad;

    @NotBlank(message = "Certificaciones son obligatorias")
    private String certificaciones;

    @NotNull(message = "Fecha de contratación es obligatoria")
    @PastOrPresent(message = "Fecha de contratación no puede ser futura")
    private LocalDate fechaContratacion;
}