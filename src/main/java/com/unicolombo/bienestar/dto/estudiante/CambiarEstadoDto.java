package com.unicolombo.bienestar.dto.estudiante;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CambiarEstadoDto {
    @NotNull(message = "El estado es obligatorio")
    @Pattern(regexp = "ACTIVO|INACTIVO|GRADUADO|SUSPENDIDO|EGRESADO",
            message = "Estado no valido")
    private String estado;
}