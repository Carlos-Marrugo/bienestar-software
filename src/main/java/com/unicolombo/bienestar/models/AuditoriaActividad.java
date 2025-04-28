package com.unicolombo.bienestar.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_actividades")
@Data
public class AuditoriaActividad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TipoAccion accion;

    @ManyToOne
    private Actividad actividad;
    @ManyToOne
    private Usuario usuario;

    private LocalDateTime fecha;
    private String detalles;
}