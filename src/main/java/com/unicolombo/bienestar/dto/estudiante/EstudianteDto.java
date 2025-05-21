package com.unicolombo.bienestar.dto.estudiante;

import com.unicolombo.bienestar.models.EstadoEstudiante;
import lombok.Data;

@Data
public class EstudianteDto {
    private Long id;
    private String nombre;
    private String apellido;
    private String nombreCompleto;
    private String codigoEstudiantil;
    private String programaAcademico;
    private Integer semestre;
    private EstadoEstudiante estado;


}