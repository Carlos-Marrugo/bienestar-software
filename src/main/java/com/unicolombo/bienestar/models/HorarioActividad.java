package com.unicolombo.bienestar.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalTime;

@Entity
@Table(name = "horarios_actividad")
@Getter
@Setter
@ToString(exclude = {"horarioBase", "actividad"})
public class HorarioActividad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @ManyToOne
    @JoinColumn(name = "horario_ubicacion_id", nullable = false)
    @JsonIgnoreProperties({"actividad", "horariosEspecificos"})
    private HorarioUbicacion horarioBase;

    @ManyToOne
    @JoinColumn(name = "actividad_id", nullable = false)
    @JsonIgnoreProperties({"horariosEspecificos", "horarios", "horarioUbicacion"})
    private Actividad actividad;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFin;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HorarioActividad that = (HorarioActividad) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}