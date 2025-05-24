package com.unicolombo.bienestar.dto.request.actividad;

import com.unicolombo.bienestar.models.AuditoriaActividad;
import com.unicolombo.bienestar.models.TipoAccion;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditoriaDto {
    private String detalles;
    private String nombreUsuario;
    private LocalDateTime fecha;
    private TipoAccion accion;

    public AuditoriaDto(AuditoriaActividad auditoria) {
        this.detalles = auditoria.getDetalles();
        this.nombreUsuario = auditoria.getUsuario().getNombre() + " " + auditoria.getUsuario().getApellido();
        this.fecha = auditoria.getFecha();
        this.accion = auditoria.getAccion();
    }
}