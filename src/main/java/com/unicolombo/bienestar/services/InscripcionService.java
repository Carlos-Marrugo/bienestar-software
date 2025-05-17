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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    /**
     * Crea una nueva inscripción de estudiante a una actividad
     * @param dto datos de la inscripción
     * @param emailUsuario email del usuario que realiza la acción
     * @return la inscripción creada
     */
    @Transactional
    @CacheEvict(value = {"inscripciones", "inscripcionesEstudiante", "inscripcionesActividad"}, allEntries = true)
    public Inscripcion crearInscripcion(InscripcionCreateDto dto, String emailUsuario) {
        log.info("Intentando inscribir estudiante {} en actividad {}", dto.getEstudianteId(), dto.getActividadId());
        // Validar existencia de actividad
        Actividad actividad = actividadRepository.findById(dto.getActividadId())
                .orElseThrow(() -> new BusinessException("Actividad no encontrada"));
        log.error("Actividad no encontrada: {}", dto.getActividadId());

        // Validar existencia de estudiante
        Estudiante estudiante = estudianteRepository.findById(dto.getEstudianteId())
                .orElseThrow(() -> new BusinessException("Estudiante no encontrado"));

        // Verificar si ya está inscrito
        if (inscripcionRepository.existsByEstudianteIdAndActividadId(dto.getEstudianteId(), dto.getActividadId())) {
            throw new BusinessException("El estudiante ya está inscrito en esta actividad");
        }

        // Verificar si hay cupos disponibles
        int inscritosActuales = inscripcionRepository.countByActividadId(dto.getActividadId());
        if (inscritosActuales >= actividad.getMaxEstudiantes()) {
            throw new BusinessException("No hay cupos disponibles para esta actividad");
        }

        // Verificar si el estudiante está activo
        if (estudiante.getEstado() != EstadoEstudiante.ACTIVO) {
            throw new BusinessException("El estudiante no está activo y no puede inscribirse");
        }

        // Crear inscripción
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setEstudiante(estudiante);
        inscripcion.setActividad(actividad);
        inscripcion.setFechaInscripcion(LocalDate.now());  // Usando LocalDate en lugar de LocalDateTime

        // Guardar la inscripción
        inscripcion = inscripcionRepository.save(inscripcion);

        // Registrar auditoría
        auditoriaService.registrarAccion(
                emailUsuario,
                TipoAccion.CREACION,
                "Inscripción de estudiante " + estudiante.getNombreCompleto() +
                        " en actividad " + actividad.getNombre(),
                inscripcion.getId()
        );

        return inscripcion;
    }

    /**
     * Obtiene una inscripción por su ID
     * @param id ID de la inscripción
     * @return la inscripción encontrada
     */
    @Cacheable(value = "inscripcion", key = "#id")
    public Inscripcion obtenerInscripcion(Long id) {
        return inscripcionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Inscripción no encontrada"));
    }

    /**
     * Lista inscripciones de un estudiante específico
     * @param estudianteId ID del estudiante
     * @param pageable paginación
     * @return página de inscripciones
     */
    @Cacheable(value = "inscripcionesEstudiante", key = "{#estudianteId, #pageable.pageNumber, #pageable.pageSize}")
    public Page<Inscripcion> listarInscripcionesPorEstudiante(Long estudianteId, Pageable pageable) {
        // Verificar existencia del estudiante
        if (!estudianteRepository.existsById(estudianteId)) {
            throw new BusinessException("Estudiante no encontrado");
        }

        return inscripcionRepository.findByEstudianteId(estudianteId, pageable);
    }

    /**
     * Lista inscripciones de una actividad específica
     * @param actividadId ID de la actividad
     * @param pageable paginación
     * @return página de inscripciones
     */
    @Cacheable(value = "inscripcionesActividad", key = "{#actividadId, #pageable.pageNumber, #pageable.pageSize}")
    public Page<Inscripcion> listarInscripcionesPorActividad(Long actividadId, Pageable pageable) {
        // Verificar existencia de la actividad
        if (!actividadRepository.existsById(actividadId)) {
            throw new BusinessException("Actividad no encontrada");
        }

        return inscripcionRepository.findByActividadId(actividadId, pageable);
    }

    /**
     * Lista inscripciones para un instructor específico
     * @param instructorId ID del instructor
     * @param pageable paginación
     * @return página de inscripciones
     */
    @Cacheable(value = "inscripcionesInstructor", key = "{#instructorId, #pageable.pageNumber, #pageable.pageSize}")
    public Page<Inscripcion> listarInscripcionesPorInstructor(Long instructorId, Pageable pageable) {
        return inscripcionRepository.findByInstructorId(instructorId, pageable);
    }

    /**
     * Cancela una inscripción
     * @param id ID de la inscripción
     * @param emailUsuario email del usuario que realiza la acción
     */
    @Transactional
    @CacheEvict(value = {"inscripciones", "inscripcion", "inscripcionesEstudiante",
            "inscripcionesActividad", "inscripcionesInstructor"}, allEntries = true)
    public void cancelarInscripcion(Long id, String emailUsuario) {
        Inscripcion inscripcion = inscripcionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Inscripción no encontrada"));

        auditoriaService.registrarAccion(
                emailUsuario,
                TipoAccion.ELIMINACION,
                "Cancelación de inscripción de estudiante " + inscripcion.getEstudiante().getNombreCompleto() +
                        " en actividad " + inscripcion.getActividad().getNombre(),
                inscripcion.getId()
        );

        inscripcionRepository.delete(inscripcion);
    }

    /**
     * Verifica si un estudiante está inscrito en una actividad
     * @param estudianteId ID del estudiante
     * @param actividadId ID de la actividad
     * @return true si está inscrito, false en caso contrario
     */
    @Cacheable(value = "verificarInscripcion", key = "{#estudianteId, #actividadId}")
    public boolean estaInscrito(Long estudianteId, Long actividadId) {
        return inscripcionRepository.existsByEstudianteIdAndActividadId(estudianteId, actividadId);
    }

    /**
     * Obtiene el conteo de inscritos en una actividad
     * @param actividadId ID de la actividad
     * @return número de inscritos
     */
    @Cacheable(value = "conteoInscripciones", key = "#actividadId")
    public int contarInscripcionesPorActividad(Long actividadId) {
        return inscripcionRepository.countByActividadId(actividadId);
    }

    /**
     * Obtiene todas las inscripciones de una actividad con estudiante y usuario
     * @param actividadId ID de la actividad
     * @return lista de inscripciones
     */
    @Cacheable(value = "listaInscripcionesActividad", key = "#actividadId")
    public List<Inscripcion> obtenerInscripcionesPorActividad(Long actividadId) {
        return inscripcionRepository.findAllByActividadIdWithEstudiante(actividadId);
    }

    /**
     * Listar todas las inscripciones (para administradores)
     * @param page número de página
     * @param size tamaño de página
     * @return página de inscripciones
     */
    @Cacheable(value = "inscripciones", key = "{#page, #size}")
    public Page<Inscripcion> listarInscripciones(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaInscripcion").descending());
        return inscripcionRepository.findAll(pageable);
    }

    /**
     * Verifica si un usuario instructor es responsable de una actividad
     * @param usuarioId ID del usuario instructor
     * @param actividadId ID de la actividad
     * @return true si es instructor de la actividad, false en caso contrario
     */
    public boolean verificarInstructorDeActividad(Long usuarioId, Long actividadId) {
        Actividad actividad = actividadRepository.findById(actividadId)
                .orElseThrow(() -> new BusinessException("Actividad no encontrada"));

        return actividad.getInstructor() != null &&
                actividad.getInstructor().getUsuario() != null &&
                actividad.getInstructor().getUsuario().getId().equals(usuarioId);
    }
}