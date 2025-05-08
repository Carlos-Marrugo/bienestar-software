package com.unicolombo.bienestar.models;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "estudiante")
@Data
public class Estudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "codigo_estudiantil", unique = true, nullable = false)
    private String codigoEstudiantil;

    @Column(name = "horas_acumuladas", nullable = false, columnDefinition = "integer default 0")
    private int horasAcumuladas = 0;

    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL)
    private List<Inscripcion> inscripciones;
}