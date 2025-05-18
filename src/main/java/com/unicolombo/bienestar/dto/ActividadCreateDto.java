package com.unicolombo.bienestar.dto;

import com.unicolombo.bienestar.models.DiaSemana;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ActividadCreateDto {
    @NotBlank(message = "El nombre es requerido")
    private String nombre;

    @NotNull(message = "La ubicación es requerida")
    private Long ubicacionId;

    @NotNull(message = "La fecha de inicio es requerida")
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @Min(value = 5, message = "La capacidad mínima es de 5 estudiantes")
    @NotNull(message = "La capacidad es requerida")
    private Integer maxEstudiantes;

    @NotNull(message = "El instructor es requerido")
    private Long instructorId;

    @NotEmpty(message = "Debe seleccionar al menos un horario")
    private List<HorarioActividadDto> horarios;

    public static class HorarioActividadDto {
        @NotNull(message = "El ID del horario es requerido")
        private Long horarioUbicacionId;

        @NotNull(message = "La hora de inicio es requerida")
        private LocalTime horaInicio;

        @NotNull(message = "La hora de fin es requerida")
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


    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Long getUbicacionId() {
        return ubicacionId;
    }

    public void setUbicacionId(Long ubicacionId) {
        this.ubicacionId = ubicacionId;
    }

    public List<HorarioActividadDto> getHorarios() {
        return horarios;
    }

    public void setHorarios(List<HorarioActividadDto> horarios) {
        this.horarios = horarios;
    }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public Integer getMaxEstudiantes() { return maxEstudiantes; }
    public void setMaxEstudiantes(Integer maxEstudiantes) { this.maxEstudiantes = maxEstudiantes; }

    public Long getInstructorId() { return instructorId; }
    public void setInstructorId(Long instructorId) { this.instructorId = instructorId; }
}