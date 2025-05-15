package com.unicolombo.bienestar.dto;

import com.unicolombo.bienestar.models.DiaSemana;
import jakarta.persistence.Column;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ActividadCreateDto {
    private String nombre;
    private List<Long> horarioUbicacionIds;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer maxEstudiantes;
    private Long instructorId;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public List<Long> getHorarioUbicacionIds() { return horarioUbicacionIds; }
    public void setHorarioUbicacionIds(List<Long> horarioUbicacionIds) { this.horarioUbicacionIds = horarioUbicacionIds; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public Integer getMaxEstudiantes() { return maxEstudiantes; }
    public void setMaxEstudiantes(Integer maxEstudiantes) { this.maxEstudiantes = maxEstudiantes; }

    public Long getInstructorId() { return instructorId; }
    public void setInstructorId(Long instructorId) { this.instructorId = instructorId; }
}
