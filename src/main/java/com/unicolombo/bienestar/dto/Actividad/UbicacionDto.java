package com.unicolombo.bienestar.dto.Actividad;

import com.unicolombo.bienestar.models.DiaSemana;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class UbicacionDto {
    @NotBlank
    private String nombre;

    @Min(1)
    private Integer capacidad;

    @Valid
    @Size(min = 1)
    private List<HorarioUbicacionDto> horarios;


}
