package com.unicolombo.bienestar.dto.estudiante;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegistroEstudianteDto {
    @NotBlank @Email(regexp = ".+@unicolombo\\.edu\\.co$")
    private String email;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$")
    private String password;

    @NotBlank @Size(max = 100)
    private String nombre;

    @NotBlank @Size(max = 100)
    private String apellido;

    @Pattern(regexp = "^\\d{8,10}$", message = "El código estudiantil debe tener entre 8 y 10 dígitos")
    private String codigoEstudiantil;

    @NotBlank @Size(max = 100)
    private String programaAcademico;

    @Min(1) @Max(12)
    private Integer semestre;
}