package com.unicolombo.bienestar.dto.request.inscripcion;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InscripcionCreateDto {
    @NotNull(message = "El ID del estudiante es obligatorio")
    private Long estudianteId;

    @NotNull(message = "El ID de la actividad es obligatoria")
    private Long actividadId;
}