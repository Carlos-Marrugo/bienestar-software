package com.unicolombo.bienestar.models;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;


@Entity
@Table(name = "suspensiones")
@Data
public class Suspension {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY)


    private Usuario administrador;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private LocalDateTime fechaFin;

}
