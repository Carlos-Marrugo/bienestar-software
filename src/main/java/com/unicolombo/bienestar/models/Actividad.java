package com.unicolombo.bienestar.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "actividad")
public class Actividad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @ManyToOne
    @JoinColumn(name = "horario_ubicacion_id")
    private HorarioUbicacion horarioUbicacion;

    @ManyToOne
    @JoinColumn(name = "ubicacion_id")
    private Ubicacion ubicacion;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer maxEstudiantes;

    @ManyToOne
    @JoinColumn(name = "instructor_id")
    private Instructor instructor;

    @ManyToMany
    @JoinTable(
            name = "actividad_horarios",
            joinColumns = @JoinColumn(name = "actividad_id"),
            inverseJoinColumns = @JoinColumn(name = "horario_id")
    )
    private Set<HorarioUbicacion> horarios = new HashSet<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public HorarioUbicacion getHorarioUbicacion() { return horarioUbicacion; }
    public void setHorarioUbicacion(HorarioUbicacion horarioUbicacion) { this.horarioUbicacion = horarioUbicacion; }

    public Ubicacion getUbicacion() { return ubicacion; }
    public void setUbicacion(Ubicacion ubicacion) { this.ubicacion = ubicacion; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public Integer getMaxEstudiantes() { return maxEstudiantes; }
    public void setMaxEstudiantes(Integer maxEstudiantes) { this.maxEstudiantes = maxEstudiantes; }

    public Instructor getInstructor() { return instructor; }
    public void setInstructor(Instructor instructor) { this.instructor = instructor; }

    public Set<HorarioUbicacion> getHorarios() { return horarios; }
    public void setHorarios(Set<HorarioUbicacion> horarios) { this.horarios = horarios; }
}