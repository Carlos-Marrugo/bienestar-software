package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.dto.InscripcionCreateDto;
import com.unicolombo.bienestar.exceptions.BusinessException;
import com.unicolombo.bienestar.models.*;
import com.unicolombo.bienestar.repositories.ActividadRepository;
import com.unicolombo.bienestar.repositories.EstudianteRepository;
import com.unicolombo.bienestar.repositories.InscripcionRepository;
import com.unicolombo.bienestar.repositories.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class InscripcionService {

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private ActividadRepository actividadRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AuditoriaService auditoriaService;

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    @CacheEvict(value = {"inscripciones", "inscripcionesEstudiante", "inscripcionesActividad"}, allEntries = true)
    public Inscripcion crearInscripcion(InscripcionCreateDto dto, String emailUsuario) {
        log.info("Intentando inscribir estudiante {} en actividad {}", dto.getEstudianteId(), dto.getActividadId());

        Actividad actividad = actividadRepository.findById(dto.getActividadId())
                .orElseThrow(() -> {
                    log.error("Actividad con ID {} no encontrada", dto.getActividadId());
                    return new BusinessException("Actividad no encontrada con ID: " + dto.getActividadId());
                });

        Estudiante estudiante = estudianteRepository.findById(dto.getEstudianteId())
                .orElseThrow(() -> {
                    log.error("Estudiante con ID {} no encontrado", dto.getEstudianteId());
                    return new BusinessException("Estudiante no encontrado con ID: " + dto.getEstudianteId());
                });

        if (inscripcionRepository.existsByEstudianteIdAndActividadId(dto.getEstudianteId(), dto.getActividadId())) {
            log.warn("El estudiante {} ya está inscrito en la actividad {}", dto.getEstudianteId(), dto.getActividadId());
            throw new BusinessException("El estudiante ya está inscrito en esta actividad");
        }

        int inscritosActuales = inscripcionRepository.countByActividadId(dto.getActividadId());
        if (inscritosActuales >= actividad.getMaxEstudiantes()) {
            log.warn("No hay cupos disponibles para la actividad {}. Cupos: {}, Inscritos: {}",
                    dto.getActividadId(), actividad.getMaxEstudiantes(), inscritosActuales);
            throw new BusinessException("No hay cupos disponibles para esta actividad");
        }

        if (estudiante.getEstado() != EstadoEstudiante.ACTIVO) {
            log.warn("El estudiante {} no está activo y no puede inscribirse", dto.getEstudianteId());
            throw new BusinessException("El estudiante no está activo y no puede inscribirse");
        }

        try {
            Inscripcion inscripcion = new Inscripcion();
            inscripcion.setEstudiante(estudiante);
            inscripcion.setActividad(actividad);
            inscripcion.setFechaInscripcion(LocalDate.now());
            inscripcion.setHorasRegistradas(0);

            inscripcion = inscripcionRepository.save(inscripcion);
            log.info("Inscripción creada exitosamente: ID {}", inscripcion.getId());

            String detalleAuditoria = "Inscripción de estudiante " + estudiante.getNombreCompleto() +
                    " en actividad " + actividad.getNombre();

            registrarAuditoriaSinActividad(emailUsuario, TipoAccion.CREACION, detalleAuditoria);

            return inscripcion;
        } catch (Exception e) {
            log.error("Error grave al guardar la inscripción: {}", e.getMessage(), e);
            throw new BusinessException("Error al procesar la inscripción: " + e.getMessage());
        }
    }

    private void registrarAuditoriaSinActividad(String emailUsuario, TipoAccion accion, String detalles) {
        try {
            auditoriaService.registrarAccion(emailUsuario, accion, detalles);
        } catch (Exception e) {
            // Solo logueamos el error sin propagarlo para evitar rollback de la transacción principal
            log.error("Error al registrar auditoría: {}", e.getMessage());
        }
    }

    @Cacheable(value = "inscripcion", key = "#id")
    public Inscripcion obtenerInscripcion(Long id) {
        return inscripcionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Inscripción no encontrada"));
    }

    @Cacheable(value = "inscripcionesEstudiante", key = "{#estudianteId, #pageable.pageNumber, #pageable.pageSize}")
    public Page<Inscripcion> listarInscripcionesPorEstudiante(Long estudianteId, Pageable pageable) {
        if (!estudianteRepository.existsById(estudianteId)) {
            throw new BusinessException("Estudiante no encontrado");
        }

        return inscripcionRepository.findByEstudianteId(estudianteId, pageable);
    }

    @Cacheable(value = "inscripcionesActividad", key = "{#actividadId, #pageable.pageNumber, #pageable.pageSize}")
    public Page<Inscripcion> listarInscripcionesPorActividad(Long actividadId, Pageable pageable) {
        if (!actividadRepository.existsById(actividadId)) {
            throw new BusinessException("Actividad no encontrada");
        }

        return inscripcionRepository.findByActividadId(actividadId, pageable);
    }

    @Cacheable(value = "inscripcionesInstructor", key = "{#instructorId, #pageable.pageNumber, #pageable.pageSize}")
    public Page<Inscripcion> listarInscripcionesPorInstructor(Long instructorId, Pageable pageable) {
        return inscripcionRepository.findByInstructorId(instructorId, pageable);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    @CacheEvict(value = {"inscripciones", "inscripcion", "inscripcionesEstudiante",
            "inscripcionesActividad", "inscripcionesInstructor"}, allEntries = true)
    public void cancelarInscripcion(Long id, String emailUsuario) {
        Inscripcion inscripcion = inscripcionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Inscripción no encontrada"));

        String detalleAuditoria = "Cancelación de inscripción de estudiante " +
                inscripcion.getEstudiante().getNombreCompleto() +
                " en actividad " + inscripcion.getActividad().getNombre();

        inscripcionRepository.delete(inscripcion);

        registrarAuditoriaSinActividad(emailUsuario, TipoAccion.ELIMINACION, detalleAuditoria);
    }

    @Cacheable(value = "verificarInscripcion", key = "{#estudianteId, #actividadId}")
    public boolean estaInscrito(Long estudianteId, Long actividadId) {
        return inscripcionRepository.existsByEstudianteIdAndActividadId(estudianteId, actividadId);
    }

    @Cacheable(value = "conteoInscripciones", key = "#actividadId")
    public int contarInscripcionesPorActividad(Long actividadId) {
        return inscripcionRepository.countByActividadId(actividadId);
    }

    @Cacheable(value = "listaInscripcionesActividad", key = "#actividadId")
    public List<Inscripcion> obtenerInscripcionesPorActividad(Long actividadId) {
        if(!actividadRepository.existsById(actividadId)) {
            log.error("Actividad con ID {} no encontrada al obtener inscripciones", actividadId);
            throw new BusinessException("Actividad no encontrada con ID: " + actividadId);
        }
        return inscripcionRepository.findAllByActividadIdWithEstudiante(actividadId);
    }

    @Cacheable(value = "inscripciones", key = "{#page, #size}")
    public Page<Inscripcion> listarInscripciones(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaInscripcion").descending());
        return inscripcionRepository.findAll(pageable);
    }

    public boolean verificarInstructorDeActividad(Long usuarioId, Long actividadId) {
        Actividad actividad = actividadRepository.findById(actividadId)
                .orElseThrow(() -> new BusinessException("Actividad no encontrada"));

        return actividad.getInstructor() != null &&
                actividad.getInstructor().getUsuario() != null &&
                actividad.getInstructor().getUsuario().getId().equals(usuarioId);
    }
}