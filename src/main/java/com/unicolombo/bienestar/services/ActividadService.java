package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.dto.ActividadCreateDto;
import com.unicolombo.bienestar.exceptions.BusinessException;
import com.unicolombo.bienestar.models.*;
import com.unicolombo.bienestar.repositories.ActividadRepository;
import com.unicolombo.bienestar.repositories.InstructorRepository;
import com.unicolombo.bienestar.repositories.UbicacionRepository;
import com.unicolombo.bienestar.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
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
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaInicio").descending());

        if (filtro != null && !filtro.isEmpty()) {
            return actividadRepository.findByNombreContainingIgnoreCase(filtro, pageable);
        }

        return actividadRepository.findAll(pageable);
    }


    public Page<Actividad> findByInstructorId(Long instructorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaInicio").descending());
        return actividadRepository.findByInstructorId(instructorId, pageable);
    }


    @Transactional
    @CacheEvict(value = {"actividades", "ubicaciones"}, allEntries = true)
    public Actividad crearActividad(ActividadCreateDto dto, String emailUsuario) {
        if (dto.getMaxEstudiantes() < 5) {
            throw new BusinessException("La capacidad mínima es de 5 estudiantes");
        }

        HorarioUbicacion horario = ubicacionRepository.findHorarioById(dto.getHorarioUbicacionId())
                .orElseThrow(() -> new BusinessException("Horario no encontrado"));

        if (dto.getFechaInicio().isBefore(LocalDate.now())) {
            throw new BusinessException("La fecha de inicio no puede ser en el pasado");
        }

        if (dto.getFechaInicio().isBefore(horario.getFechaInicio()) ||
                (dto.getFechaFin() != null && dto.getFechaFin().isAfter(horario.getFechaFin()))) {
            throw new BusinessException("Las fechas de la actividad no coinciden con el horario seleccionado");
        }

        if (dto.getMaxEstudiantes() > horario.getUbicacion().getCapacidad()) {
            throw new BusinessException("La capacidad excede el máximo de la ubicación");
        }

        Instructor instructor = instructorRepository.findById(dto.getInstructorId())
                .orElseThrow(() -> new BusinessException("Instructor no encontrado"));

        if (actividadRepository.existsSolapamientoHorario(
                dto.getHorarioUbicacionId(),
                dto.getFechaInicio(),
                dto.getFechaFin())) {
            throw new BusinessException("Ya existe una actividad en este horario");
        }

        Actividad actividad = new Actividad();
        actividad.setNombre(dto.getNombre());
        actividad.setHorarioUbicacion(horario);
        actividad.setUbicacion(horario.getUbicacion());
        actividad.setFechaInicio(dto.getFechaInicio());
        actividad.setFechaFin(dto.getFechaFin());
        actividad.setMaxEstudiantes(dto.getMaxEstudiantes());
        actividad.setInstructor(instructor);

        auditoriaService.registrarAccion(
                emailUsuario,
                TipoAccion.CREACION,
                "Actividad creada: " + actividad.getNombre(),
                actividad.getId()
        );

        return actividadRepository.save(actividad);
    }

    @Transactional
    public Actividad editarActividad(Long id, ActividadCreateDto dto, String emailUsuario) {
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Actividad no encontrada"));

        if (!actividad.getHorarioUbicacion().getId().equals(dto.getHorarioUbicacionId())) {
            throw new BusinessException("No se puede cambiar el horario de una actividad existente");
        }

        if (dto.getFechaInicio().isBefore(LocalDate.now())) {
            throw new BusinessException("No se puede mover la actividad al pasado");
        }

        if (dto.getFechaFin() != null && dto.getFechaFin().isBefore(dto.getFechaInicio())) {
            throw new BusinessException("La fecha de fin debe ser posterior a la de inicio");
        }

        actividad.setNombre(dto.getNombre());
        actividad.setFechaInicio(dto.getFechaInicio());
        actividad.setFechaFin(dto.getFechaFin());
        actividad.setMaxEstudiantes(dto.getMaxEstudiantes());

        if (!actividad.getInstructor().getId().equals(dto.getInstructorId())) {
            Instructor nuevoInstructor = instructorRepository.findById(dto.getInstructorId())
                    .orElseThrow(() -> new BusinessException("Instructor no encontrado"));
            actividad.setInstructor(nuevoInstructor);
        }

        auditoriaService.registrarAccion(
                emailUsuario,
                TipoAccion.ACTUALIZACION,
                "Actividad actualizada: " + actividad.getNombre(),
                actividad.getId()
        );

        return actividadRepository.save(actividad);
    }

    @Transactional
    public void eliminarActividad(Long id, String emailUsuario) {
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Actividad no encontrada"));

        if (!actividad.getInscripciones().isEmpty()) {
            throw new BusinessException("No se puede eliminar una actividad con estudiantes inscritos");
        }

        auditoriaService.registrarAccion(
                emailUsuario,
                TipoAccion.ELIMINACION,
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
        HorarioUbicacion horario = ubicacionRepository.findHorarioById(dto.getHorarioUbicacionId())
                .orElseThrow(() -> new BusinessException("Horario no encontrado"));

        if (actividadRepository.existsSolapamientoHorarioExcluyendoActividad(
                dto.getInstructorId(),
                dto.getHorarioUbicacionId(),
                dto.getFechaInicio(),
                dto.getFechaFin(),
                actividadIdExcluir)) {
            throw new BusinessException("El instructor ya tiene una actividad en este horario");
        }

        if (actividadRepository.existsByHorarioUbicacionAndFechaInicioBetween(
                horario,
                dto.getFechaInicio(),
                dto.getFechaFin() != null ? dto.getFechaFin() : dto.getFechaInicio())) {
            throw new BusinessException("La ubicación ya está reservada en este horario");
        }
    }

    public Actividad findById(Long id) {
        return actividadRepository.findByIdWithHorario(id)
                .orElseThrow(() -> new BusinessException("Actividad no encontrada"));
    }
}