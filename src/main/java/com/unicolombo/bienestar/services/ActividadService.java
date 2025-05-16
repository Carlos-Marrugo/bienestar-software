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
import java.util.Set;
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

        if (dto.getHorarios() == null || dto.getHorarios().isEmpty()) {
            throw new BusinessException("Debe seleccionar al menos un horario");
        }

        Ubicacion ubicacion = ubicacionRepository.findById(dto.getUbicacionId())
                .orElseThrow(() -> new BusinessException("Ubicación no encontrada"));

        for (ActividadCreateDto.HorarioActividadDto horarioAct : dto.getHorarios()) {
            HorarioUbicacion horarioUbicacion = ubicacion.getHorarios().stream()
                    .filter(h -> h.getId().equals(horarioAct.getHorarioUbicacionId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("Horario no existe en la ubicación"));

            if (horarioAct.getHoraInicio().isBefore(horarioUbicacion.getHoraInicio()) ||
                    horarioAct.getHoraFin().isAfter(horarioUbicacion.getHoraFin())) {
                throw new BusinessException(String.format(
                        "El horario de la actividad (%s - %s) excede la disponibilidad de la ubicación (%s - %s)",
                        horarioAct.getHoraInicio(), horarioAct.getHoraFin(),
                        horarioUbicacion.getHoraInicio(), horarioUbicacion.getHoraFin()
                ));
            }

            if (!horarioAct.getHoraFin().isAfter(horarioAct.getHoraInicio())) {
                throw new BusinessException("La hora de fin debe ser posterior a la hora de inicio");
            }

            validarSolapamientos(
                    horarioUbicacion.getId(),
                    horarioAct.getHoraInicio(),
                    horarioAct.getHoraFin(),
                    dto.getFechaInicio(),
                    dto.getFechaFin(),
                    null
            );
        }

        Actividad actividad = new Actividad();
        actividad.setNombre(dto.getNombre());
        actividad.setFechaInicio(dto.getFechaInicio());
        actividad.setFechaFin(dto.getFechaFin());
        actividad.setMaxEstudiantes(dto.getMaxEstudiantes());
        actividad.setUbicacion(ubicacion);

        Set<HorarioUbicacion> horarios = dto.getHorarios().stream()
                .map(h -> ubicacionRepository.findHorarioById(h.getHorarioUbicacionId())
                        .orElseThrow(() -> new BusinessException("Horario no encontrado")))
                .collect(Collectors.toSet());

        actividad.setHorarios(horarios);
        actividad.setHorarioUbicacion(horarios.iterator().next()); // Para compatibilidad

        Instructor instructor = instructorRepository.findById(dto.getInstructorId())
                .orElseThrow(() -> new BusinessException("Instructor no encontrado"));
        actividad.setInstructor(instructor);

        auditoriaService.registrarAccion(emailUsuario, TipoAccion.CREACION,
                "Actividad creada: " + actividad.getNombre());

        return actividadRepository.save(actividad);
    }

    private void validarSolapamientos(Long horarioId, LocalTime horaInicio, LocalTime horaFin,
                                      LocalDate fechaInicio, LocalDate fechaFin, Long actividadIdExcluir) {
        boolean existeSolapamiento = actividadRepository.existsSolapamiento(
                horarioId,
                horaInicio,
                horaFin,
                fechaInicio,
                fechaFin,
                actividadIdExcluir);

        if (existeSolapamiento) {
            throw new BusinessException("El horario seleccionado ya está ocupado por otra actividad");
        }
    }

    @Transactional
    public Actividad editarActividad(Long id, ActividadCreateDto dto, String emailUsuario) {
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Actividad no encontrada"));

        if (!actividad.getHorarioUbicacion().getId().equals(dto.getHorarios())) {
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