package com.unicolombo.bienestar.dto.Actividad;

import com.unicolombo.bienestar.models.Actividad;
import com.unicolombo.bienestar.models.HorarioUbicacion;
import com.unicolombo.bienestar.models.Instructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ActividadDisponibleDto {
    private Long id;
    private String nombre;
    private UbicacionDto ubicacion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer maxEstudiantes;
    private Integer cuposDisponibles;
    private InstructorDto instructor;
    private List<HorarioDto> horarios;

    @Data
    public static class UbicacionDto {
        private Long id;
        private String nombre;

        public UbicacionDto(Long id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }
    }

    @Data
    public static class InstructorDto {
        private Long id;
        private String nombre;

        public InstructorDto(Instructor instructor) {
            if (instructor != null) {
                this.id = instructor.getId();
                this.nombre = instructor.getNombreCompleto();
            }
        }
    }

    @Data
    public static class HorarioDto {
        private String dia;
        private LocalTime horaInicio;
        private LocalTime horaFin;

        public HorarioDto(String dia, LocalTime horaInicio, LocalTime horaFin) {
            this.dia = dia;
            this.horaInicio = horaInicio;
            this.horaFin = horaFin;
        }
    }

    public ActividadDisponibleDto(Actividad actividad, int inscripcionesActuales) {
        this.id = actividad.getId();
        this.nombre = actividad.getNombre();

        if (actividad.getUbicacion() != null) {
            this.ubicacion = new UbicacionDto(
                    actividad.getUbicacion().getId(),
                    actividad.getUbicacion().getNombre()
            );
        }

        this.fechaInicio = actividad.getFechaInicio();
        this.fechaFin = actividad.getFechaFin();
        this.maxEstudiantes = actividad.getMaxEstudiantes();
        this.cuposDisponibles = actividad.getMaxEstudiantes() - inscripcionesActuales;
        this.instructor = new InstructorDto(actividad.getInstructor());

        if (actividad.getUbicacion() != null && actividad.getUbicacion().getHorarios() != null) {
            this.horarios = actividad.getUbicacion().getHorarios().stream()
                    .map(horario -> {
                        String diaStr = horario.getDia() != null ? horario.getDia().toString() : "No definido";
                        return new HorarioDto(
                                diaStr,
                                horario.getHoraInicio(),
                                horario.getHoraFin()
                        );
                    })
                    .collect(Collectors.toList());
        }
    }
}