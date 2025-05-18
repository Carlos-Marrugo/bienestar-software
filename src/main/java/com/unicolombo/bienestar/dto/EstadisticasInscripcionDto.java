package com.unicolombo.bienestar.dto;

import lombok.Data;
import java.util.List;

@Data
public class EstadisticasInscripcionDto {
    private Long actividadId;
    private String nombreActividad;
    private Integer capacidadTotal;
    private Integer inscritosTotales;
    private Integer cuposDisponibles;
    private Double porcentajeOcupacion;
}