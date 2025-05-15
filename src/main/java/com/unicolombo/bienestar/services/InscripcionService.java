package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.exceptions.ActividadLlenaException;
import com.unicolombo.bienestar.models.Actividad;
import com.unicolombo.bienestar.models.Estudiante;
import com.unicolombo.bienestar.models.Inscripcion;
import com.unicolombo.bienestar.repositories.ActividadRepository;
import com.unicolombo.bienestar.repositories.EstudianteRepository;
import com.unicolombo.bienestar.repositories.InscripcionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class InscripcionService {

    private final InscripcionRepository inscripcionRepository;
    private final ActividadRepository actividadRepository;
    private final EstudianteRepository estudianteRepository;

    @Autowired
    public InscripcionService(InscripcionRepository inscripcionRepository,
                              ActividadRepository actividadRepository,
                              EstudianteRepository estudianteRepository) {
        this.inscripcionRepository = inscripcionRepository;
        this.actividadRepository = actividadRepository;
        this.estudianteRepository = estudianteRepository;
    }

    public void inscribirEstudianteEnActividad(Long estudianteId, Long actividadId) {
        Actividad actividad = actividadRepository.findById(actividadId)
                .orElseThrow(() -> new IllegalArgumentException("Actividad no encontrada"));

        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));

        Long inscritos = inscripcionRepository.countInscritosByActividadId(actividadId);

        if (inscritos >= actividad.getMaxEstudiantes()) {
            throw new ActividadLlenaException("No se puede inscribir. La actividad ha alcanzado su límite de estudiantes.");
        }

        // Validación opcional: no permitir doble inscripción
        boolean yaInscrito = inscripcionRepository.existsByActividadIdAndEstudianteId(actividadId, estudianteId);
        if (yaInscrito) {
            throw new ActividadLlenaException("El estudiante ya está inscrito en esta actividad.");
        }

        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setActividad(actividad);
        inscripcion.setEstudiante(estudiante);
        inscripcion.setFechaInscripcion(LocalDate.now());
        inscripcion.setHorasRegistradas(0);

        inscripcionRepository.save(inscripcion);
    }
}
