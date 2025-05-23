package com.unicolombo.bienestar.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class RegistrarHorasDto {
    @NotNull(message = "El ID del estudiante es requerido")
    private Long estudianteId;

    @NotNull(message = "Las horas son requeridas")
    @Min(value = 1, message = "Las horas deben ser al menos 1")
    private Integer horas;

    private String descripcion;
}