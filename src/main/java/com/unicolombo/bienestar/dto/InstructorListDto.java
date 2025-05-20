package com.unicolombo.bienestar.dto;

import com.unicolombo.bienestar.models.Instructor;

import java.time.LocalDate;

public class InstructorListDto {
    private Long id;
    private String nombreCompleto;
    private String especialidad;
    private LocalDate fechaContratacion;

    // Constructor que toma un Instructor
    public InstructorListDto(Instructor instructor) {
        this.id = instructor.getId();
        this.nombreCompleto = instructor.getNombreCompleto();
        this.especialidad = instructor.getEspecialidad();
        this.fechaContratacion = instructor.getFechaContratacion();
    }

    // Getters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
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
}