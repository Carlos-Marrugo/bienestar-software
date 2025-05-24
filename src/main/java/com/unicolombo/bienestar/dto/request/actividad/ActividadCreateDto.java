package com.unicolombo.bienestar.dto.request.actividad;

import jakarta.validation.constraints.*;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@ToString
public class ActividadCreateDto {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String nombre;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @FutureOrPresent(message = "La fecha de inicio debe ser hoy o en el futuro")
    private LocalDate fechaInicio;

    @FutureOrPresent(message = "La fecha de fin debe ser hoy o en el futuro")
    private LocalDate fechaFin;

    @NotNull(message = "La capacidad máxima es obligatoria")
    @Min(value = 5, message = "La capacidad mínima es de 5 estudiantes")
    private Integer maxEstudiantes;

    @NotNull(message = "El ID de la ubicación es obligatorio")
    private Long ubicacionId;

    @NotNull(message = "El ID del instructor es obligatorio")
    private Long instructorId;

    @NotEmpty(message = "Debe incluir al menos un horario")
    private List<HorarioActividadDto> horarios;

    public static class HorarioActividadDto {
        @NotNull(message = "El ID del horario base es obligatorio")
        private Long horarioUbicacionId;

        @NotNull(message = "La hora de inicio es obligatoria")
        private LocalTime horaInicio;

        @NotNull(message = "La hora de fin es obligatoria")
        private LocalTime horaFin;

        public Long getHorarioUbicacionId() {
            return horarioUbicacionId;
        }

        public void setHorarioUbicacionId(Long horarioUbicacionId) {
            this.horarioUbicacionId = horarioUbicacionId;
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
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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

    public Integer getMaxEstudiantes() {
        return maxEstudiantes;
    }

    public void setMaxEstudiantes(Integer maxEstudiantes) {
        this.maxEstudiantes = maxEstudiantes;
    }

    public Long getUbicacionId() {
        return ubicacionId;
    }

    public void setUbicacionId(Long ubicacionId) {
        this.ubicacionId = ubicacionId;
    }

    public Long getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(Long instructorId) {
        this.instructorId = instructorId;
    }

    public List<HorarioActividadDto> getHorarios() {
        return horarios;
    }

    public void setHorarios(List<HorarioActividadDto> horarios) {
        this.horarios = horarios;
    }
}