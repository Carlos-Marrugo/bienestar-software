package com.unicolombo.bienestar.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "inscripcion")
@Data
public class Inscripcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "estudiante_id", nullable = false)
    @JsonIgnoreProperties("inscripciones")
    private Estudiante estudiante;

    @ManyToOne
    @JoinColumn(name = "actividad_id", nullable = false)
    @JsonIgnoreProperties("inscripciones")
    private Actividad actividad;

    @Column(name = "horas_registradas", nullable = false)
    private int horasRegistradas = 0;

    @Column(name = "fecha_inscripcion", nullable = false)
    private LocalDate fechaInscripcion = LocalDate.now();

    /*
    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("inscripcion")
    private List<RegistroAsistencia> asistencias;*/
}