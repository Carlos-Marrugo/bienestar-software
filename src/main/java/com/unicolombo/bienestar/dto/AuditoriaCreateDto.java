package com.unicolombo.bienestar.dto;

import com.unicolombo.bienestar.models.Actividad;
import com.unicolombo.bienestar.models.TipoAccion;
import com.unicolombo.bienestar.models.Usuario;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditoriaCreateDto {
    private String detalles;
    private TipoAccion accion;
    private Usuario usuario;
    private LocalDateTime fecha;
    private Actividad actividad;

    public AuditoriaCreateDto(String detalles, TipoAccion accion, Usuario usuario, LocalDateTime fecha, Actividad actividad) {
        this.detalles = detalles;
        this.accion = accion;
        this.usuario = usuario;
        this.fecha = fecha;
        this.actividad = actividad;
    }

    public String getDetalles() {
        return detalles;
    }

    public void setDetalles(String detalles) {
        this.detalles = detalles;
    }

    public TipoAccion getAccion() {
        return accion;
    }

    public void setAccion(TipoAccion accion) {
        this.accion = accion;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Actividad getActividad() {
        return actividad;
    }

    public void setActividad(Actividad actividad) {
        this.actividad = actividad;
    }
}
