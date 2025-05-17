package com.unicolombo.bienestar.dto.Actividad;

import com.unicolombo.bienestar.models.DiaSemana;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public  class HorarioUbicacionDto {
    @NotNull
    private DiaSemana dia;

    @NotNull
    private LocalTime horaInicio;

    @NotNull
    private LocalTime horaFin;

    @NotNull
    private LocalDate fechaInicio;

    @NotNull
    private LocalDate fechaFin;


}
