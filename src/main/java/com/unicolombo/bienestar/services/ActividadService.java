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
import java.util.HashSet;
import java.util.Set;

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

        Ubicacion ubicacion = ubicacionRepository.findByIdWithHorarios(dto.getUbicacionId())
                .orElseThrow(() -> new BusinessException("Ubicación no encontrada"));

        Set<HorarioUbicacion> horariosActividad = new HashSet<>();

        for (ActividadCreateDto.HorarioActividadDto horarioDto : dto.getHorarios()) {
            HorarioUbicacion horarioBase = ubicacion.getHorarios().stream()
                    .filter(h -> h.getId().equals(horarioDto.getHorarioUbicacionId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(
                            String.format("Horario base con ID %d no encontrado en la ubicación",
                                    horarioDto.getHorarioUbicacionId())));

            if (horarioDto.getHoraInicio().isBefore(horarioBase.getHoraInicio()) ||
                    horarioDto.getHoraFin().isAfter(horarioBase.getHoraFin())) {
                throw new BusinessException(String.format(
                        "El horario (%s-%s) debe estar dentro del rango base (%s-%s) del horario %d",
                        horarioDto.getHoraInicio(), horarioDto.getHoraFin(),
                        horarioBase.getHoraInicio(), horarioBase.getHoraFin(),
                        horarioBase.getId()
                ));
            }

            if (!horarioDto.getHoraFin().isAfter(horarioDto.getHoraInicio())) {
                throw new BusinessException("La hora de fin debe ser posterior a la hora de inicio");
            }

            validarSolapamientos(
                    horarioBase.getId(),
                    horarioDto.getHoraInicio(),
                    horarioDto.getHoraFin(),
                    dto.getFechaInicio(),
                    dto.getFechaFin(),
                    null
            );

            horariosActividad.add(horarioBase);
        }

        Actividad actividad = new Actividad();
        actividad.setNombre(dto.getNombre());
        actividad.setFechaInicio(dto.getFechaInicio());
        actividad.setFechaFin(dto.getFechaFin());
        actividad.setMaxEstudiantes(dto.getMaxEstudiantes());
        actividad.setUbicacion(ubicacion);
        actividad.setHorarios(horariosActividad);

        if (!horariosActividad.isEmpty()) {
            actividad.setHorarioUbicacion(horariosActividad.iterator().next());
        }

        Instructor instructor = instructorRepository.findById(dto.getInstructorId())
                .orElseThrow(() -> new BusinessException("Instructor no encontrado"));
        actividad.setInstructor(instructor);

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

        if (!actividad.getHorarioUbicacion().getId().equals(dto.getHorarios().get(0).getHorarioUbicacionId())) {
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

        // Validación de inscripciones (comentada por ahora)
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

    public boolean existeSolapamientoHorario(Long instructorId, LocalDate fecha,
                                             LocalTime horaInicio, LocalTime horaFin,
                                             Long actividadIdExcluir) {
        return actividadRepository.existsByInstructorIdAndFechaInicioAndHoraInicioLessThanAndHoraFinGreaterThanAndIdNot(
                instructorId, fecha, horaFin, horaInicio, actividadIdExcluir);
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