package com.unicolombo.bienestar.dto.estudiante;

import com.unicolombo.bienestar.models.EstadoEstudiante;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ActualizarEstudianteDto {
    @Size(max = 100)
    private String nombre;

    @Size(max = 100)
    private String apellido;

    @NotBlank
    @Size(max = 100)
    private String programaAcademico;

    @Min(1) @Max(12)
    private Integer semestre;

    @NotNull
    private EstadoEstudiante estado;


}