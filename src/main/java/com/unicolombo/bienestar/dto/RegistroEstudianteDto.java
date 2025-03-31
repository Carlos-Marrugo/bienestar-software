package com.unicolombo.bienestar.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegistroEstudianteDto {
    private String email;
    private String password;
    private String nombre;
    private String apellido;
    private String codigoEstudiantil;
}