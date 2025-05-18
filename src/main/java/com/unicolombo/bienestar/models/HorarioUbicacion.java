package com.unicolombo.bienestar.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "horarios_ubicacion")
@Getter
@Setter
@ToString(exclude = {"ubicacion", "actividades"})
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

    @ManyToOne
    @JoinColumn(name = "ubicacion_id", nullable = false)
    @JsonBackReference
    private Ubicacion ubicacion;

    @OneToMany(mappedBy = "horarioUbicacion")
    @JsonIgnore
    private List<Actividad> actividades = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HorarioUbicacion that = (HorarioUbicacion) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}