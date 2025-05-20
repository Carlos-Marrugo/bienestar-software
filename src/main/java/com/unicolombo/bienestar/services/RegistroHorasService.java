package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.controllers.InscripcionController;
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
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistroHorasService {

    private final AsistenciaRepository asistenciaRepository;
    private final RegistroHorasRepository registroHorasRepository;
    private final InscripcionRepository inscripcionRepository;
    private final EstudianteRepository estudianteRepository;
    private final InstructorRepository instructorRepository;
    private final AuditoriaService auditoriaService;
    private final ActividadRepository actividadRepository;

    @Transactional
    public Long registrarAsistencia(RegistroAsistenciaDto dto, String emailInstructor) {

        Instructor instructor = validarInstructor(emailInstructor);

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
        asistencia.setHoras(0);
        asistencia.setInscripcion(inscripcion);
        asistencia.setInstructor(instructor);

        Asistencia asistenciaGuardada = asistenciaRepository.save(asistencia);


        auditoriaService.registrarAccion(
                emailInstructor,
                TipoAccion.CREACION,
                "Asistencia registrada para: " + estudiante.getNombreCompleto(),
                actividad.getId()
        );

        log.info("Asistencia registrada para estudiante ID: {} en actividad ID: {}",
                estudiante.getId(), actividad.getId());

        return asistenciaGuardada.getId();
    }

    @Transactional
    public void registrarHoras(RegistroHorasDto dto, String emailInstructor) {

        Instructor instructor = validarInstructor(emailInstructor);


        Asistencia asistencia = asistenciaRepository.findById(dto.getAsistenciaId())
                .orElseThrow(() -> new BusinessException("Asistencia no encontrada"));

        if (!asistencia.getInstructor().getId().equals(instructor.getId())) {
            throw new BusinessException("No puedes registrar horas en esta asistencia");
        }

        if (!asistencia.isConfirmada()) {
            throw new BusinessException("No se pueden registrar horas sin asistencia confirmada");
        }

        Inscripcion inscripcion = asistencia.getInscripcion();
        Estudiante estudiante = inscripcion.getEstudiante();
        Actividad actividad = inscripcion.getActividad();

        RegistroHoras registroHoras = new RegistroHoras();
        registroHoras.setHoras(dto.getHoras());
        registroHoras.setDescripcion(dto.getDescripcion());
        registroHoras.setAsistencia(asistencia);
        registroHoras.setInstructor(instructor);
        registroHoras.setActividad(actividad);
        registroHoras.setEstudiante(estudiante);

        registroHorasRepository.save(registroHoras);

        asistencia.setHoras(asistencia.getHoras() + dto.getHoras());
        asistenciaRepository.save(asistencia);

        inscripcion.setHorasRegistradas(inscripcion.getHorasRegistradas() + dto.getHoras());
        inscripcionRepository.save(inscripcion);

        estudiante.setHorasAcumuladas(estudiante.getHorasAcumuladas() + dto.getHoras());
        estudianteRepository.save(estudiante);

        auditoriaService.registrarAccion(
                emailInstructor,
                TipoAccion.ACTUALIZACION,
                String.format("%d horas registradas para estudiante %s: %s",
                        dto.getHoras(), estudiante.getNombreCompleto(), dto.getDescripcion()),
                actividad.getId()
        );

        log.info("{} horas registradas para asistencia ID: {}", dto.getHoras(), dto.getAsistenciaId());
    }

    public boolean verificarInscripcion(Long actividadId, Long estudianteId, String emailInstructor) {
        Instructor instructor = validarInstructor(emailInstructor);

        Actividad actividad = actividadRepository.findById(actividadId)
                .orElseThrow(() -> new BusinessException("Actividad no encontrada"));

        if (!actividad.getInstructor().getId().equals(instructor.getId())) {
            throw new BusinessException("No eres el instructor de esta actividad");
        }

        return inscripcionRepository.existsByEstudianteIdAndActividadId(estudianteId, actividadId);
    }

    public Optional<Asistencia> buscarAsistenciaActual(Long actividadId, Long estudianteId, String emailInstructor) {
        Instructor instructor = validarInstructor(emailInstructor);

        Actividad actividad = actividadRepository.findById(actividadId)
                .orElseThrow(() -> new BusinessException("Actividad no encontrada"));

        if (!actividad.getInstructor().getId().equals(instructor.getId())) {
            throw new BusinessException("No eres el instructor de esta actividad");
        }

        if (!inscripcionRepository.existsByEstudianteIdAndActividadId(estudianteId, actividadId)) {
            throw new BusinessException("El estudiante no está inscrito en esta actividad");
        }

        return asistenciaRepository.findByEstudianteIdAndActividadIdAndFecha(
                estudianteId, actividadId, LocalDate.now());
    }

    private Instructor validarInstructor(String emailInstructor) {
        return instructorRepository.findByUsuarioEmail(emailInstructor)
                .orElseThrow(() -> new BusinessException("Instructor no encontrado"));
    }

    public List<Asistencia> obtenerAsistenciasEstudiante(Long actividadId, Long estudianteId, String emailInstructor) {
        Instructor instructor = validarInstructor(emailInstructor);

        Actividad actividad = actividadRepository.findById(actividadId)
                .orElseThrow(() -> new BusinessException("Actividad no encontrada"));

        if (!actividad.getInstructor().getId().equals(instructor.getId())) {
            throw new BusinessException("No eres el instructor de esta actividad");
        }

        if (!inscripcionRepository.existsByEstudianteIdAndActividadId(estudianteId, actividadId)) {
            throw new BusinessException("El estudiante no está inscrito en esta actividad");
        }

        return asistenciaRepository.findAllByEstudianteIdAndActividadId(estudianteId, actividadId);
    }
}