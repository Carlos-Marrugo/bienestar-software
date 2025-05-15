package com.unicolombo.bienestar.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "actividades", indexes = {
        @Index(name = "idx_actividad_fecha", columnList = "fecha_inicio"),
        @Index(name = "idx_actividad_ubicacion", columnList = "ubicacion_id"),
        @Index(name = "idx_actividad_instructor", columnList = "instructor_id")
})
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ubicacion_id", nullable = false)
    private Ubicacion ubicacion;

    @Column(nullable = false, name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaFin;


    @Column(nullable = false, name = "max_estudiantes")
    private Integer maxEstudiantes;

    @ManyToOne
    @JsonIgnoreProperties("actividades")
    @JoinColumn(name = "instructor_id", referencedColumnName = "id")
    private Instructor instructor;

    @OneToMany(mappedBy = "actividad", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("actividad")
    private List<AuditoriaActividad> auditorias = new ArrayList<>();

    @OneToMany(mappedBy = "actividad", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("actividad")
    private List<Inscripcion> inscripciones = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "horario_ubicacion_id")
    @JsonIgnoreProperties("actividades")
    private HorarioUbicacion horarioUbicacion;


    public Actividad() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(Ubicacion ubicacion) {
        this.ubicacion = ubicacion;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Integer getMaxEstudiantes() {
        return maxEstudiantes;
    }

    public void setMaxEstudiantes(Integer maxEstudiantes) {
        this.maxEstudiantes = maxEstudiantes;
    }

    public Instructor getInstructor() {
        return instructor;
    }

    public void setInstructor(Instructor instructor) {
        this.instructor = instructor;
    }

    public List<AuditoriaActividad> getAuditorias() {
        return auditorias;
    }

    public void setAuditorias(List<AuditoriaActividad> auditorias) {
        this.auditorias = auditorias;
    }

    public List<Inscripcion> getInscripciones() {
        return inscripciones;
    }

    public void setInscripciones(List<Inscripcion> inscripciones) {
        this.inscripciones = inscripciones;
    }

    public HorarioUbicacion getHorarioUbicacion() {
        return horarioUbicacion;
    }

    public void setHorarioUbicacion(HorarioUbicacion horarioUbicacion) {
        this.horarioUbicacion = horarioUbicacion;
    }
}
