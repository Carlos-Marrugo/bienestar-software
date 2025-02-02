package com.unicolombo.bienestar.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Lugar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false)
    private int capacidad;

    @Column(nullable = false)
    private boolean activo = true;
}