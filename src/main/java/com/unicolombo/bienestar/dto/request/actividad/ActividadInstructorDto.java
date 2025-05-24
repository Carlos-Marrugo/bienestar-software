package com.unicolombo.bienestar.dto.request.actividad;

import com.unicolombo.bienestar.models.Actividad;
import com.unicolombo.bienestar.models.HorarioActividad;
import com.unicolombo.bienestar.models.Ubicacion;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ActividadInstructorDto {
    private Long id;
    private String nombre;
    private UbicacionSimpleDto ubicacion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer maxEstudiantes;
    private List<HorarioAsignadoDto> horariosAsignados;

    @Data
    public static class UbicacionSimpleDto {
        private Long id;
        private String nombre;
        private Integer capacidad;

        public UbicacionSimpleDto(Ubicacion ubicacion) {
            this.id = ubicacion.getId();
            this.nombre = ubicacion.getNombre();
            this.capacidad = ubicacion.getCapacidad();
        }
    }

    @Data
    public static class HorarioAsignadoDto {
        private Long id;
        private String dia;
        private LocalTime horaInicio;
        private LocalTime horaFin;

        public HorarioAsignadoDto(HorarioActividad horario) {
            this.id = horario.getId();
            this.dia = horario.getHorarioBase().getDia().toString();
            this.horaInicio = horario.getHoraInicio();
            this.horaFin = horario.getHoraFin();
        }
    }

    public ActividadInstructorDto(Actividad actividad) {
        this.id = actividad.getId();
        this.nombre = actividad.getNombre();
        this.ubicacion = new UbicacionSimpleDto(actividad.getUbicacion());
        this.fechaInicio = actividad.getFechaInicio();
        this.fechaFin = actividad.getFechaFin();
        this.maxEstudiantes = actividad.getMaxEstudiantes();
        this.horariosAsignados = actividad.getHorariosEspecificos().stream()
                .map(HorarioAsignadoDto::new)
                .collect(Collectors.toList());
    }
}