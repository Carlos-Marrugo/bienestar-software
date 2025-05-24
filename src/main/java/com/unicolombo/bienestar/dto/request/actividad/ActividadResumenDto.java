package com.unicolombo.bienestar.dto.request.actividad;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ActividadResumenDto {
    private Long id;
    private String nombre;
    private String ubicacion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer horasRegistradas;
    private Integer maxEstudiantes;
}