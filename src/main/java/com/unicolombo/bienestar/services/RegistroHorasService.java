package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.dto.RegistroAsistenciaDto;
import com.unicolombo.bienestar.dto.RegistroHorasDto;
import com.unicolombo.bienestar.exceptions.BusinessException;
import com.unicolombo.bienestar.models.*;
import com.unicolombo.bienestar.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistroHorasService {

    private final AsistenciaRepository asistenciaRepository;
    private final InscripcionRepository inscripcionRepository;
    private final EstudianteRepository estudianteRepository;
    private final InstructorRepository instructorRepository;
    private final AuditoriaService auditoriaService;
    private final ActividadRepository actividadRepository;

    @Transactional
    public Long registrarAsistencia(RegistroAsistenciaDto dto, String emailInstructor) {
        Instructor instructor = instructorRepository.findByUsuarioEmail(emailInstructor)
                .orElseThrow(() -> new BusinessException("Instructor no encontrado"));

        Actividad actividad = actividadRepository.findById(dto.getActividadId())
                .orElseThrow(() -> new BusinessException("Actividad no encontrada"));

        if (!actividad.getInstructor().getId().equals(instructor.getId())) {
            throw new BusinessException("No eres el instructor de esta actividad");
        }

        Estudiante estudiante = estudianteRepository.findById(dto.getEstudianteId())
                .orElseThrow(() -> new BusinessException("Estudiante no encontrado"));

        Inscripcion inscripcion = inscripcionRepository
                .findByEstudianteIdAndActividadId(dto.getEstudianteId(), dto.getActividadId())
                .orElseThrow(() -> new BusinessException("El estudiante no está inscrito en esta actividad"));

        Asistencia asistencia = new Asistencia();
        asistencia.setConfirmada(true);
        asistencia.setFecha(LocalDate.now());
        asistencia.setHoras(dto.getHoras());
        asistencia.setInscripcion(inscripcion);
        asistencia.setInstructor(instructor);

        Asistencia asistenciaGuardada = asistenciaRepository.save(asistencia);

        inscripcion.setHorasRegistradas(inscripcion.getHorasRegistradas() + dto.getHoras());
        inscripcionRepository.save(inscripcion);

        estudiante.setHorasAcumuladas(estudiante.getHorasAcumuladas() + dto.getHoras());
        estudianteRepository.save(estudiante);

        auditoriaService.registrarAccion(
                emailInstructor,
                TipoAccion.CREACION,
                "Asistencia registrada para: " + estudiante.getNombreCompleto(),
                asistenciaGuardada.getId()
        );

        return asistenciaGuardada.getId();
    }

    @Transactional
    public void registrarHoras(RegistroHorasDto dto, String emailInstructor) {
        Instructor instructor = instructorRepository.findByUsuarioEmail(emailInstructor)
                .orElseThrow(() -> new BusinessException("Instructor no encontrado"));

        Asistencia asistencia = asistenciaRepository.findById(dto.getAsistenciaId())
                .orElseThrow(() -> new BusinessException("Asistencia no encontrada"));

        if (!asistencia.getInstructor().getId().equals(instructor.getId())) {
            throw new BusinessException("No puedes registrar horas en esta asistencia");
        }

        if (!asistencia.isConfirmada()) {
            throw new BusinessException("No se pueden registrar horas sin asistencia confirmada");
        }

        asistencia.setHoras(dto.getHoras());
        asistenciaRepository.save(asistencia);

        Inscripcion inscripcion = asistencia.getInscripcion();
        inscripcion.setHorasRegistradas(inscripcion.getHorasRegistradas() + dto.getHoras());
        inscripcionRepository.save(inscripcion);

        Estudiante estudiante = inscripcion.getEstudiante();
        estudiante.setHorasAcumuladas(estudiante.getHorasAcumuladas() + dto.getHoras());
        estudianteRepository.save(estudiante);

        auditoriaService.registrarAccion(
                emailInstructor,
                TipoAccion.ACTUALIZACION,
                String.format("%d horas registradas para estudiante %s",
                        dto.getHoras(), estudiante.getNombreCompleto()),
                asistencia.getId()
        );

        log.info("{} horas registradas para asistencia ID: {}", dto.getHoras(), dto.getAsistenciaId());
    }

    public boolean verificarInscripcion(Long actividadId, Long estudianteId, String emailInstructor) {
        Instructor instructor = instructorRepository.findByUsuarioEmail(emailInstructor)
                .orElseThrow(() -> new BusinessException("Instructor no encontrado"));

        Actividad actividad = actividadRepository.findById(actividadId)
                .orElseThrow(() -> new BusinessException("Actividad no encontrada"));

        if (!actividad.getInstructor().getId().equals(instructor.getId())) {
            throw new BusinessException("No eres el instructor de esta actividad");
        }

        return inscripcionRepository.existsByEstudianteIdAndActividadId(estudianteId, actividadId);
    }
}