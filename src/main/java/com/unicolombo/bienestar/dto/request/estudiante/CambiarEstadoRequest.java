package com.unicolombo.bienestar.dto.request.estudiante;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CambiarEstadoRequest {
    @NotNull
    private Boolean activa;

    public Boolean getActiva() {
        return activa;
    }
}