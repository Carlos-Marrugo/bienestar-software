package com.unicolombo.bienestar.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ubicaciones")
@Data
public class Ubicacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private Integer capacidad;

    @Column(nullable = false)
    private Boolean activa = true;

    @OneToMany(mappedBy = "ubicacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("ubicacion")
    private List<HorarioUbicacion> horarios = new ArrayList<>();

    @OneToMany(mappedBy = "ubicacion")
    @JsonIgnoreProperties("ubicacion")
    private List<Actividad> actividades = new ArrayList<>();


}