package com.unicolombo.bienestar.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistroHorasDto {
    @NotNull(message = "El ID de la asistencia es obligatorio")
    private Long asistenciaId;

    @NotNull(message = "El número de horas es obligatorio")
    @Min(value = 1, message = "El número de horas debe ser mayor a 0")
    private Integer horas;

    @NotBlank(message = "La descripción de la actividad es obligatoria")
    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String descripcion;
}