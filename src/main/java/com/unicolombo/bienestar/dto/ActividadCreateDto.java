package com.unicolombo.bienestar.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ActividadCreateDto {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "Ubicacion es obligatoria")
    private String ubicacion;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;

    private LocalTime horaFin;

    @NotNull(message = "El máximo de estudiantes es obligatorio")
    @Min(value = 1, message = "Debe tener al menos 1 estudiante")
    private Integer maxEstudiantes;

    @NotNull(message = "El ID del instructor es obligatorio")
    private Long instructorId;

}
