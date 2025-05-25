package com.unicolombo.bienestar.dto.request.instructor;

import com.unicolombo.bienestar.models.Instructor;
import lombok.Data;

@Data
public class InstructorPerfilDto {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String especialidad;
    private String fechaContratacion;

    public InstructorPerfilDto(Instructor instructor) {
        this.id = instructor.getId();
        this.nombre = instructor.getUsuario().getNombre();
        this.apellido = instructor.getUsuario().getApellido();
        this.email = instructor.getUsuario().getEmail();
        this.especialidad = instructor.getEspecialidad();
        this.fechaContratacion = instructor.getFechaContratacion().toString();
    }
}