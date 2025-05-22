package com.unicolombo.bienestar.dto.estudiante;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class EstudianteInscritoDto {
    private Long id;
    private String codigoEstudiantil;
    private String nombreCompleto;
    private String programaAcademico;
    private Integer semestre;
    private LocalDateTime fechaInscripcion;
}