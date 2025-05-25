package com.unicolombo.bienestar.dto.request.instructor;

import com.unicolombo.bienestar.models.Instructor;

import java.time.LocalDate;

public class InstructorDetailDto {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String especialidad;
    private LocalDate fechaContratacion;
    private String fotoPerfil;

    public InstructorDetailDto(Instructor instructor) {
        this.id = instructor.getId();
        this.nombre = instructor.getUsuario().getNombre();
        this.apellido = instructor.getUsuario().getApellido();
        this.email = instructor.getUsuario().getEmail();
        this.especialidad = instructor.getEspecialidad();
        this.fechaContratacion = instructor.getFechaContratacion();
        this.fotoPerfil = instructor.getFotoPerfil();
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

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }
}