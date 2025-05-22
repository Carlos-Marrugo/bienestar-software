package com.unicolombo.bienestar.dto.estudiante;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class EstudianteInscritoDto {
    private Long id;
    private String codigoEstudiantil;
    private String nombreCompleto;
    private String programaAcademico;
    private Integer semestre;
    private LocalDate fechaInscripcion;
    private Integer horasAcumuladas;
}
