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


    @CacheEvict(value = {"actividades", "ubicaciones"}, allEntries = true)
    @Transactional
    public Actividad crearActividad(ActividadCreateDto dto, String emailUsuario) {
        if (dto.getMaxEstudiantes() < 5) {
            throw new BusinessException("La capacidad mínima es de 5 estudiantes");
        }

        if (dto.getFechaInicio().isBefore(LocalDate.now())) {
            throw new BusinessException("La fecha de inicio no puede ser en el pasado");
        }

        if (dto.getFechaFin() != null && dto.getFechaFin().isBefore(dto.getFechaInicio())) {
            throw new BusinessException("La fecha de fin debe ser posterior a la fecha de inicio");
        }

        Ubicacion ubicacion = ubicacionRepository.findById(dto.getUbicacionId())
                .orElseThrow(() -> new BusinessException("Ubicación no encontrada"));

        if (!dto.getFechaInicio().getDayOfWeek().equals(dto.getDia().getDayOfWeek())) {
            throw new BusinessException("La fecha no coincide con el día de la semana especificado");
        }

        ubicacionService.validarDisponibilidad(
                dto.getUbicacionId(),
                dto.getFechaInicio(),
                dto.getHoraInicio(),
                dto.getHoraFin()
        );

        Instructor instructor = instructorRepository.findById(dto.getInstructorId())
                .orElseThrow(() -> new BusinessException("Instructor no encontrado"));

        if (!instructor.getUsuario().isActivo()) {
            throw new BusinessException("El instructor está inactivo");
        }

        validarSolapamiento(dto, null);

        Actividad actividad = new Actividad();
        actividad.setNombre(dto.getNombre());
        actividad.setUbicacion(ubicacion); // Establecer la ubicación
        actividad.setFechaInicio(dto.getFechaInicio());
        actividad.setFechaFin(dto.getFechaFin());
        actividad.setHoraInicio(dto.getHoraInicio());
        actividad.setHoraFin(dto.getHoraFin());
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