package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.dto.ActividadCreateDto;
import com.unicolombo.bienestar.exceptions.BusinessException;
import com.unicolombo.bienestar.models.*;
import com.unicolombo.bienestar.repositories.ActividadRepository;
import com.unicolombo.bienestar.repositories.InstructorRepository;
import com.unicolombo.bienestar.repositories.UbicacionRepository;
import com.unicolombo.bienestar.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;


@Service
@Slf4j
public class ActividadService {

    @Autowired
    private ActividadRepository actividadRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AuditoriaService auditoriaService;

    @Autowired
    private UbicacionService ubicacionService;

    @Autowired
    private UbicacionRepository ubicacionRepository;

    public Page<Actividad> listarActividadesAdmin(int page, int size, String filtro) {
        if (page < 0) throw new IllegalArgumentException("El número de página no puede ser negativo");
        if (size <= 0 || size > 100) throw new IllegalArgumentException("El tamaño de página debe estar entre 1 y 100");

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<Actividad> actividades;

        if (filtro != null && !filtro.isEmpty()) {
            if (!filtro.matches("^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ ]+$")) {
                throw new IllegalArgumentException("Filtro contiene caracteres no permitidos");
            }
            actividades = actividadRepository.findByNombreContainingIgnoreCase(filtro, pageable);
        } else {
            actividades = actividadRepository.findAll(pageable);
        }

        actividades.getContent().forEach(act -> {
            if (act.getInstructor() == null || act.getInstructor().getUsuario() == null) {
                log.warn("Actividad con ID {} tiene instructor o usuario nulo", act.getId());
            }
        });

        return actividades;
    }


    public Page<Actividad> findByInstructorId(Long instructorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaInicio").descending());
        return actividadRepository.findByInstructorId(instructorId, pageable);
    }

    @Transactional
    public Actividad crearActividad(ActividadCreateDto dto, String emailUsuario) {
        Ubicacion ubicacion = ubicacionRepository.findById(dto.getUbicacionId())
                .orElseThrow(() -> new BusinessException("Ubicación no encontrada"));

        if (!dto.getFechaInicio().getDayOfWeek().equals(dto.getDia())) {
            throw new BusinessException("La fecha no coincide con el día de la semana especificado");
        }

        validarSolapamiento(dto, null);

        ubicacionService.validarDisponibilidad(
                dto.getUbicacionId(),
                dto.getFechaInicio(),
                dto.getHoraInicio(),
                dto.getHoraFin()
        );

        Instructor instructor = instructorRepository.findById(dto.getInstructorId())
                .orElseThrow(() -> {
                    log.error("Instructor no encontrado con ID: {}", dto.getInstructorId());
                    return new BusinessException("Instructor no encontrado");
                });

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));


        if (instructor.getUsuario().getRol() != Role.INSTRUCTOR) {
            log.warn("Usuario con ID {} no es instructor", dto.getInstructorId());
            throw new BusinessException("El usuario no tiene rol de instructor");
        }

        if (dto.getMaxEstudiantes() < 5) {
            throw new BusinessException("La capacidad mínima es de 5 estudiantes");
        }

        if (dto.getFechaFin() != null && dto.getFechaFin().isBefore(dto.getFechaInicio())) {
            throw new BusinessException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }

        if (dto.getHoraFin() != null && dto.getHoraFin().isBefore(dto.getHoraInicio())) {
            throw new BusinessException("La hora de fin no puede ser anterior a la hora de inicio");
        }

        if (existeSolapamientoHorario(dto.getInstructorId(), dto.getFechaInicio(),
                dto.getHoraInicio(), dto.getHoraFin(), dto.getInstructorId())) {
            throw new BusinessException("El instructor ya tiene una actividad programada en ese horario");
        }

        Actividad actividad = new Actividad();
        actividad.setNombre(dto.getNombre());
        actividad.setUbicacion(ubicacion);
        actividad.setFechaInicio(dto.getFechaInicio());
        actividad.setFechaFin(dto.getFechaFin());
        actividad.setHoraInicio(dto.getHoraInicio());
        actividad.setHoraFin(dto.getHoraFin());
        actividad.setMaxEstudiantes(dto.getMaxEstudiantes());
        actividad.setInstructor(instructor);

        log.info("Guardando nueva actividad: {}", actividad.getNombre());

        auditoriaService.registrarAccion(
                usuario.getEmail(),
                TipoAccion.CREACION,
                "Actividad creada: " + actividad.getNombre(),
                actividad.getId()
        );
        return actividadRepository.save(actividad);
    }

    @Transactional
    public Actividad editarActividad(Long id, ActividadCreateDto dto, String emailUsuario) {
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Actividad no encontrada con id: "+id));

        Ubicacion ubicacion = ubicacionRepository.findById(dto.getUbicacionId())
                .orElseThrow(() -> new BusinessException("Ubicación no encontrada"));

        validarSolapamiento(dto, id); // Validar solapamiento excluyendo esta actividad

        ubicacionService.validarDisponibilidad(
                dto.getUbicacionId(),
                dto.getFechaInicio(),
                dto.getHoraInicio(),
                dto.getHoraFin()
        );

        actividad.setNombre(dto.getNombre());
        actividad.setUbicacion(ubicacion); // Usar la ubicación obtenida
        actividad.setFechaInicio(dto.getFechaInicio());
        actividad.setFechaFin(dto.getFechaFin());
        actividad.setHoraInicio(dto.getHoraInicio());
        actividad.setHoraFin(dto.getHoraFin());
        actividad.setMaxEstudiantes(dto.getMaxEstudiantes());

        if(!actividad.getInstructor().getId().equals(dto.getInstructorId())) {
            Instructor nuevoInstructor = instructorRepository.findById(dto.getInstructorId())
                    .orElseThrow(() -> new BusinessException("Instructor no encontrado"));
            actividad.setInstructor(nuevoInstructor);
        }

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        auditoriaService.registrarAccion(
                usuario.getEmail(),
                TipoAccion.ACTUALIZACION,
                "Actividad actualizada: " + actividad.getNombre(),
                actividad.getId()
        );

        return actividadRepository.save(actividad);
    }

    @Transactional
    public void eliminarActividad(Long id, String emailUsuario) {
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Actividad no encontrada con id: " + id));

        if (!actividad.getInscripciones().isEmpty()) {
            throw new BusinessException("No se puede eliminar una actividad con estudiantes inscritos");
        }

        auditoriaService.registrarAccion(
                emailUsuario,
                TipoAccion.valueOf("ELIMINACION"),
                "Actividad eliminada: " + actividad.getNombre(),
                actividad.getId()
        );

        actividadRepository.delete(actividad);
    }


    public boolean existeSolapamientoHorario(Long instructorId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, Long actividadIdExcluir) {
        return actividadRepository.existsByInstructorIdAndFechaInicioAndHoraInicioLessThanAndHoraFinGreaterThanAndIdNot(
                instructorId, fecha, horaFin, horaInicio, actividadIdExcluir);
    }

    private void validarSolapamiento(ActividadCreateDto dto, Long actividadIdExcluir) {
        if (actividadRepository.existsSolapamientoHorarioExcluyendoActividad(
                dto.getInstructorId(),
                dto.getFechaInicio(),
                dto.getHoraInicio(),
                dto.getHoraFin(),
                actividadIdExcluir)) {
            throw new BusinessException("El instructor ya tiene una actividad en este horario");
        }

        if (ubicacionRepository.estaOcupada(
                dto.getUbicacionId(),
                dto.getFechaInicio(),
                dto.getHoraInicio(),
                dto.getHoraFin())) {
            throw new BusinessException("La ubicación ya está reservada en este horario");
        }
    }

    public Actividad findById(Long id) {
        return actividadRepository.findById(1L)
                .orElseThrow(()-> new BusinessException("Actividad no encontrada con id: "+id));
    }
}