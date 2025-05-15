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
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;


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
    public Actividad crearActividad(ActividadCreateDto dto, String emailUsuario) {
        if (dto.getMaxEstudiantes() == null || dto.getMaxEstudiantes() < 5) {
            throw new BusinessException("La capacidad mínima es de 5 estudiantes");
        }

        if (dto.getFechaInicio() == null) {
            throw new BusinessException("La fecha de inicio es requerida");
        }

        List<HorarioUbicacion> horarios = dto.getHorarioUbicacionIds().stream()
                .map(id -> ubicacionRepository.findHorarioById(id)
                        .orElseThrow(() -> new BusinessException("Horario no encontrado con ID: " + id)))
                .collect(Collectors.toList());

        if (horarios.isEmpty()) {
            throw new BusinessException("Debe seleccionar al menos un horario");
        }

        Long ubicacionId = horarios.get(0).getUbicacion().getId();
        if (horarios.stream().anyMatch(h -> !h.getUbicacion().getId().equals(ubicacionId))) {
            throw new BusinessException("Todos los horarios deben ser de la misma ubicación");
        }

        for (HorarioUbicacion horario : horarios) {
            if (dto.getFechaInicio().isBefore(horario.getFechaInicio())) {
                throw new BusinessException("La fecha de inicio no puede ser anterior al horario asignado");
            }
            if (dto.getFechaFin() != null && horario.getFechaFin() != null &&
                    dto.getFechaFin().isAfter(horario.getFechaFin())) {
                throw new BusinessException("La fecha de fin no puede ser posterior al horario asignado");
            }
            if (dto.getMaxEstudiantes() > horario.getUbicacion().getCapacidad()) {
                throw new BusinessException("La capacidad excede el máximo de la ubicación");
            }
        }

        for (HorarioUbicacion horario : horarios) {
            validarSolapamientos(dto, horario.getId(), null);
        }

        Actividad actividad = new Actividad();
        actividad.setNombre(dto.getNombre());
        actividad.setFechaInicio(dto.getFechaInicio());
        actividad.setFechaFin(dto.getFechaFin());
        actividad.setMaxEstudiantes(dto.getMaxEstudiantes());

        actividad.setHorarioUbicacion(horarios.get(0));
        actividad.setUbicacion(horarios.get(0).getUbicacion());

        actividad.setHorarios(new HashSet<>(horarios));

        Instructor instructor = instructorRepository.findById(dto.getInstructorId())
                .orElseThrow(() -> new BusinessException("Instructor no encontrado"));
        actividad.setInstructor(instructor);

        auditoriaService.registrarAccion(emailUsuario, TipoAccion.CREACION,
                "Actividad creada: " + actividad.getNombre());

        return actividadRepository.save(actividad);
    }

    @Transactional
    public Actividad editarActividad(Long id, ActividadCreateDto dto, String emailUsuario) {
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Actividad no encontrada"));

        if (!actividad.getHorarioUbicacion().getId().equals(dto.getHorarioUbicacionIds())) {
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

        /*if (!actividad.getInscripciones().isEmpty()) {
            throw new BusinessException("No se puede eliminar una actividad con estudiantes inscritos");
        }*/

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

    private void validarSolapamientos(ActividadCreateDto dto, Long horarioId, Long actividadIdExcluir) {
        List<Actividad> actividadesSolapadas = actividadRepository.findSolapamientos(
                horarioId,
                dto.getFechaInicio(),
                dto.getFechaFin(),
                actividadIdExcluir);

        if (!actividadesSolapadas.isEmpty()) {
            Actividad conflicto = actividadesSolapadas.get(0);
            throw new BusinessException(
                    String.format("El horario ya está ocupado por la actividad '%s' (ID: %d) del instructor %s",
                            conflicto.getNombre(),
                            conflicto.getId(),
                            conflicto.getInstructor().getNombreCompleto())
            );
        }

        List<Actividad> actividadesInstructor = actividadRepository.findSolapamientosInstructor(
                dto.getInstructorId(),
                horarioId,
                dto.getFechaInicio(),
                dto.getFechaFin(),
                actividadIdExcluir);

        if (!actividadesInstructor.isEmpty()) {
            Actividad conflicto = actividadesInstructor.get(0);
            throw new BusinessException(
                    String.format("El instructor ya tiene la actividad '%s' (ID: %d) programada en este horario",
                            conflicto.getNombre(),
                            conflicto.getId())
            );
        }
    }

    public Actividad findById(Long id) {
        Actividad actividad = actividadRepository.findByIdWithHorarios(id)
                .orElseThrow(() -> new BusinessException("Actividad no encontrada"));

        if (actividad.getHorarioUbicacion() == null && !actividad.getHorarios().isEmpty()) {
            actividad.setHorarioUbicacion(actividad.getHorarios().iterator().next());
        }

        return actividad;
    }
}