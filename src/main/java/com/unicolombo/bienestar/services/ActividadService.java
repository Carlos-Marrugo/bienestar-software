package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.dto.ActividadCreateDto;
import com.unicolombo.bienestar.models.Actividad;
import com.unicolombo.bienestar.models.Role;
import com.unicolombo.bienestar.models.Usuario;
import com.unicolombo.bienestar.repositories.ActividadRepository;
import com.unicolombo.bienestar.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class ActividadService {

    @Autowired
    private ActividadRepository actividadRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public Actividad crearActividad(ActividadCreateDto dto) {
        // obtener instructor
        Usuario instructor = usuarioRepository.findById(dto.getInstructorId())
                .orElseThrow(() -> new RuntimeException("Instructor no encontrado con ID: " + dto.getInstructorId()));

        // verificacion si es un instructor
        if (instructor.getRol() != Role.INSTRUCTOR) {
            throw new RuntimeException("El usuario con ID " + dto.getInstructorId() + " no es un instructor");
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

        return actividadRepository.save(actividad);
    }


    //editar actividad
    public Actividad editarActividad(Long id, ActividadCreateDto dto){
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada con id: "+id));

        actividad.setNombre(dto.getNombre());
        actividad.setUbicacion(dto.getUbicacion());
        actividad.setFechaInicio(dto.getFechaInicio());
        actividad.setFechaFin(dto.getFechaFin());
        actividad.setHoraInicio(dto.getHoraInicio());
        actividad.setHoraFin(dto.getHoraFin());
        actividad.setMaxEstudiantes(dto.getMaxEstudiantes());

        if(!actividad.getInstructor().getId().equals(dto.getInstructorId())){
            Usuario nuevoInstructor = usuarioRepository.findById(dto.getInstructorId())
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

}