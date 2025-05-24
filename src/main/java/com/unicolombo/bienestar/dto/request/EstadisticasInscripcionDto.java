package com.unicolombo.bienestar.dto.request;

import lombok.Data;

@Data
public class EstadisticasInscripcionDto {
    private Long actividadId;
    private String nombreActividad;
    private Integer capacidadTotal;
    private Integer inscritosTotales;
    private Integer cuposDisponibles;
    private Double porcentajeOcupacion;
}