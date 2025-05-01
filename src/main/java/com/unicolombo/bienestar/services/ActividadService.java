package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.dto.ActividadCreateDto;
import com.unicolombo.bienestar.exceptions.BusinessException;
import com.unicolombo.bienestar.models.Actividad;
import com.unicolombo.bienestar.models.Instructor;
import com.unicolombo.bienestar.models.Role;
import com.unicolombo.bienestar.models.Usuario;
import com.unicolombo.bienestar.repositories.ActividadRepository;
import com.unicolombo.bienestar.repositories.InstructorRepository;
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

    //paginacion
    public Page<Actividad> listarActividadesAdmin(int page, int size, String filtro) {

        if (page < 0) throw new IllegalArgumentException("El número de página no puede ser negativo");
        if (size <= 0 || size > 100) throw new IllegalArgumentException("El tamaño de página debe estar entre 1 y 100");

        Pageable pageable = PageRequest.of(page, size, Sort.by("ubicacion").descending());

        if (filtro != null && !filtro.isEmpty()) {
            if (!filtro.matches("^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ ]+$")) {
                throw new IllegalArgumentException("Filtro contiene caracteres no permitidos");
            }
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
        // obtener instructor
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

        if (existeSolapamientoHorario(dto.getInstructorId(), dto.getFechaInicio(), dto.getHoraInicio(), dto.getHoraFin())) {
            throw new BusinessException("El instructor ya tiene una actividad programada en ese horario");
        }

        Actividad actividad = new Actividad();
        actividad.setNombre(dto.getNombre());
        actividad.setUbicacion(dto.getUbicacion());
        actividad.setFechaInicio(dto.getFechaInicio());
        actividad.setFechaFin(dto.getFechaFin());
        actividad.setHoraInicio(dto.getHoraInicio());
        actividad.setHoraFin(dto.getHoraFin());
        actividad.setMaxEstudiantes(dto.getMaxEstudiantes());
        actividad.setInstructor(instructor);

        log.info("Guardando nueva actividad: {}", actividad.getNombre());

        auditoriaService.registrarAccion(actividad,
                usuario,
                "CREACION",
                "Actividad creada: " + actividad.getNombre()
        );
        return actividadRepository.save(actividad);
    }

    //editar actividad
    public Actividad editarActividad(Long id, ActividadCreateDto dto) {
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada con id: "+id));

        actividad.setNombre(dto.getNombre());
        actividad.setUbicacion(dto.getUbicacion());
        actividad.setFechaInicio(dto.getFechaInicio());
        actividad.setFechaFin(dto.getFechaFin());
        actividad.setHoraInicio(dto.getHoraInicio());
        actividad.setHoraFin(dto.getHoraFin());
        actividad.setMaxEstudiantes(dto.getMaxEstudiantes());

        if(!actividad.getInstructor().getId().equals(dto.getInstructorId())) {
            Instructor nuevoInstructor = instructorRepository.findById(dto.getInstructorId())
                    .orElseThrow(() -> new RuntimeException("Instructor no encontrado"));
            actividad.setInstructor(nuevoInstructor);
        }
        return actividadRepository.save(actividad);
    }

    //eliminar
    public void eliminarActividad(Long id) {
        if(!actividadRepository.existsById(id)){
            throw new RuntimeException("Actividad no encontrada con id: "+id);
        }
        actividadRepository.deleteById(id);
    }


    public boolean existeSolapamientoHorario(Long instructorId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        return actividadRepository.existsSolapamientoHorario(
                instructorId,
                fecha,
                horaInicio,
                horaFin);
    }

}