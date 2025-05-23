package com.unicolombo.bienestar.dto.Actividad;

import com.unicolombo.bienestar.models.Actividad;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ActividadDisponibleDto {
    private Long id;
    private String nombre;
    private String ubicacion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer cuposDisponibles;
    private String instructor;
    private List<String> horarios;

    public ActividadDisponibleDto(Actividad actividad, int inscripcionesActuales) {
        this.id = actividad.getId();
        this.nombre = actividad.getNombre();

        this.ubicacion = actividad.getUbicacion() != null ?
                actividad.getUbicacion().getNombre() :
                "Sin ubicación";

        this.fechaInicio = actividad.getFechaInicio();
        this.fechaFin = actividad.getFechaFin();
        this.cuposDisponibles = actividad.getMaxEstudiantes() - inscripcionesActuales;

        this.instructor = "Sin instructor asignado";
        if (actividad.getInstructor() != null &&
                actividad.getInstructor().getUsuario() != null) {
            this.instructor = actividad.getInstructor().getUsuario().getNombre() + " " +
                    actividad.getInstructor().getUsuario().getApellido();
        }

        this.horarios = actividad.getHorariosEspecificos() != null ?
                actividad.getHorariosEspecificos().stream()
                        .map(horario -> {
                            String dia = "Día no definido";
                            if (horario.getHorarioBase() != null &&
                                    horario.getHorarioBase().getDia() != null) {
                                dia = horario.getHorarioBase().getDia().toString();
                            }

                            return String.format("%s: %s - %s",
                                    dia,
                                    horario.getHoraInicio() != null ? horario.getHoraInicio() : "--:--",
                                    horario.getHoraFin() != null ? horario.getHoraFin() : "--:--");
                        })
                        .collect(Collectors.toList()) :
                List.of();
    }
}