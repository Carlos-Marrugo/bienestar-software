package com.unicolombo.bienestar.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "estudiantes")
@Data
public class Estudiante {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "codigo_estudiantil", unique = true, nullable = false, length = 8)
    private String codigoEstudiantil;

    @Column(name = "horas_acumuladas", nullable = false)
    private Integer horasAcumuladas = 0;

    @Column(nullable = false, length = 100)
    private String programaAcademico;

    @Column(nullable = false)
    private Integer semestre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEstudiante estado = EstadoEstudiante.ACTIVO;

    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("estudiante")
    private List<Inscripcion> inscripciones;

    public String getNombreCompleto() {
        return usuario.getNombre() + " " + usuario.getApellido();
    }
}
