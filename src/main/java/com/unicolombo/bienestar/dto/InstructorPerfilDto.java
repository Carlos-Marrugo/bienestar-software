package com.unicolombo.bienestar.dto;

import lombok.Data;

@Data
public class InstructorPerfilDto {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String especialidad;
    private String fechaContratacion;

}