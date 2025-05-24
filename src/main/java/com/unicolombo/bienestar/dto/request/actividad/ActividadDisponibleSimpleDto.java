package com.unicolombo.bienestar.dto.request.actividad;

import com.unicolombo.bienestar.models.Actividad;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ActividadDisponibleSimpleDto {
    private Long id;
    private String nombre;
    private String ubicacion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer maxEstudiantes;
    private Integer inscripcionesActuales;
    private Integer cuposDisponibles;
    private String instructor;

    public ActividadDisponibleSimpleDto(Actividad actividad, int inscripcionesActuales) {
        this.id = actividad.getId();
        this.nombre = actividad.getNombre();
        this.fechaInicio = actividad.getFechaInicio();
        this.fechaFin = actividad.getFechaFin();
        this.maxEstudiantes = actividad.getMaxEstudiantes();
        this.inscripcionesActuales = inscripcionesActuales;
        this.cuposDisponibles = actividad.getMaxEstudiantes() - inscripcionesActuales;

        this.ubicacion = actividad.getUbicacion() != null ?
                actividad.getUbicacion().getNombre() : "Sin ubicación";

        this.instructor = "Por asignar";
        if (actividad.getInstructor() != null) {
            if (actividad.getInstructor().getUsuario() != null) {
                this.instructor = actividad.getInstructor().getUsuario().getNombre() + " " +
                        actividad.getInstructor().getUsuario().getApellido();
            } else {
                this.instructor = "ID: " + actividad.getInstructor().getId();
            }
        }
    }
}