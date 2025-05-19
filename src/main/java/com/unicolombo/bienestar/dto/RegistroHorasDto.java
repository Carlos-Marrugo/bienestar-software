package com.unicolombo.bienestar.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class RegistroHorasDto {
    @NotNull(message = "El ID de asistencia es requerido")
    private Long asistenciaId;

    @NotNull(message = "Las horas son requeridas")
    @Min(value = 1, message = "Las horas deben ser al menos 1")
    private Integer horas;

    private String descripcion;
}
