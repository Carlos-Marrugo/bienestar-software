package com.unicolombo.bienestar.dto.request.actividad;

import com.unicolombo.bienestar.models.DiaSemana;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class HorarioDto {

    @NotNull(message = "El día es obligatorio")
    private DiaSemana dia;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;

    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime horaFin;

    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaFin;
}