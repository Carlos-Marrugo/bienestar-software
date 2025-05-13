package com.unicolombo.bienestar.dto.Actividad;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UbicacionDto {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad mínima es 1")
    private Integer capacidad;

    @NotNull(message = "Debe incluir al menos un horario")
    @Size(min = 1, message = "Debe incluir al menos un horario")
    private List<HorarioDto> horarios;
}
