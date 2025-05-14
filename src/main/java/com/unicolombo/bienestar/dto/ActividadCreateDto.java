package com.unicolombo.bienestar.dto;


import com.unicolombo.bienestar.models.DiaSemana;
import com.unicolombo.bienestar.models.Ubicacion;
import jakarta.persistence.Column;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ActividadCreateDto {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotNull(message = "Ubicacion es obligatoria")
    @Column(name = "ubicacion_id") // Añade esta anotación
    private Long ubicacionId;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;

    private LocalTime horaFin;

    @AssertTrue(message = "La fecha de fin debe ser posterior a la fecha de inicio")
    public boolean isFechaFinValid() {
        return fechaFin == null || !fechaFin.isBefore(fechaInicio);
    }

    @AssertTrue(message = "La hora de fin debe ser posterior a la hora de inicio")
    public boolean isHoraFinValid() {
        if (horaFin == null || horaInicio == null) return true;
        return !horaFin.isBefore(horaInicio);
    }

    @NotNull(message = "El maximo de estudiantes es obligatorio")
    @Min(value = 1, message = "Debe tener al menos 1 estudiante")
    private Integer maxEstudiantes;

    @NotNull(message = "El ID del instructor es obligatorio")
    private Long instructorId;

    @NotNull
    private DiaSemana dia;

    public ActividadCreateDto(String nombre, Long ubicacionId, LocalDate fechaInicio, LocalDate fechaFin, LocalTime horaInicio, LocalTime horaFin, Integer maxEstudiantes, Long instructorId) {
        this.nombre = nombre;
        this.ubicacionId = ubicacionId;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.maxEstudiantes = maxEstudiantes;
        this.instructorId = instructorId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getUbicacion() {
        return ubicacionId;
    }

    public void setUbicacion(Long ubicacionId) {
        this.ubicacionId = ubicacionId;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public Integer getMaxEstudiantes() {
        return maxEstudiantes;
    }

    public void setMaxEstudiantes(Integer maxEstudiantes) {
        this.maxEstudiantes = maxEstudiantes;
    }

    public Long getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(Long instructorId) {
        this.instructorId = instructorId;
    }
}
