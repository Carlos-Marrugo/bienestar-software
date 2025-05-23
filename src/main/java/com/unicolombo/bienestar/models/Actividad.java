package com.unicolombo.bienestar.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "actividad")
@Getter
@Setter
@ToString(exclude = {"horarios", "horariosEspecificos"})
public class Actividad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @ManyToOne
    @JoinColumn(name = "horario_ubicacion_id")
    @JsonIgnoreProperties({"actividades", "horariosEspecificos"})
    private HorarioUbicacion horarioUbicacion;

    @ManyToOne
    @JoinColumn(name = "ubicacion_id")
    private Ubicacion ubicacion;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer maxEstudiantes;

    @ManyToOne
    @JoinColumn(name = "instructor_id")
    @JsonIgnoreProperties({"usuario", "actividades"})
    private Instructor instructor;

    @ManyToMany
    @JoinTable(
            name = "actividad_horarios",
            joinColumns = @JoinColumn(name = "actividad_id"),
            inverseJoinColumns = @JoinColumn(name = "horario_id")
    )
    @JsonIgnoreProperties({"ubicacion", "actividades"})
    private Set<HorarioUbicacion> horarios = new HashSet<>();

    @OneToMany(mappedBy = "actividad", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"actividad", "horarioBase"})
    private Set<HorarioActividad> horariosEspecificos = new HashSet<>();

    @Transient
    @JsonIgnore
    private List<String> warnings = new ArrayList<>();

    public void addWarning(String warning) {
        if (this.warnings == null) {
            this.warnings = new ArrayList<>();
        }
        this.warnings.add(warning);
    }

    public List<String> getWarnings() {
        return warnings != null ? warnings : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Actividad actividad = (Actividad) o;
        return id != null && id.equals(actividad.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }


}