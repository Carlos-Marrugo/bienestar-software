package com.unicolombo.bienestar.dto.estudiante;

import com.unicolombo.bienestar.models.EstadoEstudiante;
import lombok.Data;

@Data
public class EstudiantePerfilDto {
    private Long id;
    private String nombreCompleto;
    private String email;
    private String codigoEstudiantil;
    private String programaAcademico;
    private Integer semestre;
    private Integer horasAcumuladas;
    private EstadoEstudiante estado;
    //private List<ActividadResumenDto> actividades;
}