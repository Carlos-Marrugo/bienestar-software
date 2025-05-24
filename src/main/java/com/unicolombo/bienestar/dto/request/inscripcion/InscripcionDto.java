package com.unicolombo.bienestar.dto.request.inscripcion;

import com.unicolombo.bienestar.dto.request.actividad.ActividadResumenDto;
import com.unicolombo.bienestar.dto.request.estudiante.EstudianteDto;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InscripcionDto {
    private Long id;
    private EstudianteDto estudiante;
    private ActividadResumenDto actividad;
    private LocalDateTime fechaInscripcion;
    private Integer horasRegistradas;
}