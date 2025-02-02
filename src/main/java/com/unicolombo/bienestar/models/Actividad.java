package com.unicolombo.bienestar.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class Actividad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "instructor_id", nullable = false)
    private Usuario instructor;

    @ManyToOne
    @JoinColumn(name = "coordinador_id", nullable = false)
    private Usuario coordinador;

    @ManyToOne
    @JoinColumn(name = "lugar_id", nullable = false)
    private Lugar lugar;

    @Column(nullable = false)
    private int cupoMaximo;

    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    @Column(nullable = false)
    private LocalDateTime fechaFin;

    @Column(nullable = false)
    private boolean activa = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}