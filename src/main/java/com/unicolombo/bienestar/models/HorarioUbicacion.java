package com.unicolombo.bienestar.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "horarios_ubicacion")
@Data
public class HorarioUbicacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiaSemana dia;

    @Column(nullable = false, name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(nullable = false, name = "hora_fin")
    private LocalTime horaFin;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @OneToMany(mappedBy = "horarioUbicacion")
    @JsonIgnoreProperties("horarioUbicacion")
    private List<Actividad> actividades = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "ubicacion_id", nullable = false)
    @JsonIgnoreProperties("horarios")
    private Ubicacion ubicacion;
}