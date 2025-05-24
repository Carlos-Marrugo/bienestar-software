package com.unicolombo.bienestar.dto.request.estudiante;

import com.unicolombo.bienestar.models.EstadoEstudiante;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CambiarEstadoDto {
    @NotNull(message = "El estado es obligatorio")
    private EstadoEstudiante estado;

    @Size(max = 255, message = "La observación no puede exceder de 255 caracteres")
    private String motivo;

    private LocalDateTime fechaFin;
}