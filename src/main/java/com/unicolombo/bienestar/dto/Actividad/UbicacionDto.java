package com.unicolombo.bienestar.dto.Actividad;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class UbicacionDto {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @Min(value = 1, message = "La capacidad mínima es 1")
    private Integer capacidad;

    private List<HorarioDto> horarios;
}
