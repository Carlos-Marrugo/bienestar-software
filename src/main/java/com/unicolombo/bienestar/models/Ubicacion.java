package com.unicolombo.bienestar.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ubicaciones")
@Getter
@Setter
@ToString(exclude = {"horarios", "actividades"})
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
    @JsonIgnoreProperties({"ubicacion", "actividades"})
    private List<HorarioUbicacion> horarios = new ArrayList<>();

    @OneToMany(mappedBy = "ubicacion")
    @JsonIgnore
    private List<Actividad> actividades = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ubicacion ubicacion = (Ubicacion) o;
        return id != null && id.equals(ubicacion.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}