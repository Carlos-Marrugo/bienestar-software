package com.unicolombo.bienestar.dto.Actividad;

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
    private Ubicacion ubicacion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer maxEstudiantes;
    private List<HorarioDto> horariosAsignados;

    @Data
    public static class HorarioDto {
        private Long id;
        private LocalTime horaInicio;
        private LocalTime horaFin;

        public HorarioDto(HorarioActividad horario) {
            this.id = horario.getId();
            this.horaInicio = horario.getHoraInicio();
            this.horaFin = horario.getHoraFin();
        }
    }

    public ActividadInstructorDto(Actividad actividad) {
        this.id = actividad.getId();
        this.nombre = actividad.getNombre();
        this.ubicacion = actividad.getUbicacion();
        this.fechaInicio = actividad.getFechaInicio();
        this.fechaFin = actividad.getFechaFin();
        this.maxEstudiantes = actividad.getMaxEstudiantes();
        this.horariosAsignados = actividad.getHorariosEspecificos().stream()
                .map(HorarioDto::new)
                .collect(Collectors.toList());
    }
}