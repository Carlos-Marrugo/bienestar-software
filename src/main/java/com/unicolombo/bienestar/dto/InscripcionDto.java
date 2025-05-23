package com.unicolombo.bienestar.dto;

import com.unicolombo.bienestar.dto.Actividad.ActividadResumenDto;
import com.unicolombo.bienestar.dto.estudiante.EstudianteDto;
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