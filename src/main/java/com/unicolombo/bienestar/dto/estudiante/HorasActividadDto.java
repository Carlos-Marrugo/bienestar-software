package com.unicolombo.bienestar.dto.estudiante;

import lombok.Data;

@Data
public class HorasActividadDto {
    private Long actividadId;
    private String nombreActividad;
    private String ubicacion;
    private Integer horasRegistradas;
    private Integer horasTotales;
    private String estado;
}
