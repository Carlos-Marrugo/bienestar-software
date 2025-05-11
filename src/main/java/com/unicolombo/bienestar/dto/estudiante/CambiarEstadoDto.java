package com.unicolombo.bienestar.dto.estudiante;

import com.unicolombo.bienestar.models.EstadoEstudiante;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CambiarEstadoDto {
    @NotNull(message = "El estado es obligatorio")
    @Pattern(regexp = "ACTIVO|INACTIVO|GRADUADO|SUSPENDIDO|EGRESADO",
            message = "Estado no valido")
    private EstadoEstudiante estado;


    @Size(max = 255, message = "La observacion no puede exceder de 255 caracteres")
    private String motivo;

    private LocalDateTime fechaFin;

    public EstadoEstudiante getEstado() {
        return this.estado;
    }
}