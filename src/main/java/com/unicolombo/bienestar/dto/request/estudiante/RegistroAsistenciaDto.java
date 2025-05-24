package com.unicolombo.bienestar.dto.request.estudiante;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegistroAsistenciaDto {
    @NotNull(message = "El ID del estudiante es obligatorio")
    private Long estudianteId;

    @NotNull(message = "El ID de la actividad es obligatorio")
    private Long actividadId;
}