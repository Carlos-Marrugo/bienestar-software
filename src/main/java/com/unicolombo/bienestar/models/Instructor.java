package com.unicolombo.bienestar.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "instructor")
@Data
public class Instructor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JsonIgnoreProperties("instructor")
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false)
    private String especialidad;

    @Column(nullable = false)
    private LocalDate fechaContratacion;

    @OneToMany(mappedBy = "instructor", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"instructor", "asistencias"})
    private List<Actividad> actividades = new ArrayList<>();


    public String getNombreCompleto() {
        return usuario.getNombre() + " " + usuario.getApellido();
    }

    @OneToMany(mappedBy = "instructor")
    @JsonIgnoreProperties({"instructor", "actividad"})
    private List<Asistencia> asistenciasRegistradas = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public LocalDate getFechaContratacion() {
        return fechaContratacion;
    }

    public void setFechaContratacion(LocalDate fechaContratacion) {
        this.fechaContratacion = fechaContratacion;
    }

    public List<Actividad> getActividades() {
        return actividades;
    }

    public void setActividades(List<Actividad> actividades) {
        this.actividades = actividades;
    }

    public List<Asistencia> getAsistenciasRegistradas() {
        return asistenciasRegistradas;
    }

    public void setAsistenciasRegistradas(List<Asistencia> asistenciasRegistradas) {
        this.asistenciasRegistradas = asistenciasRegistradas;
    }
}