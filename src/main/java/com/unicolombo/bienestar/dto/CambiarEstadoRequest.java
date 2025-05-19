package com.unicolombo.bienestar.dto;

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