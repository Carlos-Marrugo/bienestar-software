package com.unicolombo.bienestar.dto.estudiante;

import com.unicolombo.bienestar.models.EstadoEstudiante;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class EstudiantePerfilDto {
    private Long id;
    private String nombre;
    private String apellido;
    private String nombreCompleto;
    private String email;
    private String codigoEstudiantil;
    private String programaAcademico;
    private Integer semestre;
    private Integer horasAcumuladas;
    private EstadoEstudiante estado;
    private List<ActividadInscritaDto> actividades;

    @Data
    public static class ActividadInscritaDto {
        private Long id;
        private String nombre;
        private String ubicacion;
        private Integer horasRegistradas;
        private List<LocalDate> fechasAsistencia;
    }
}