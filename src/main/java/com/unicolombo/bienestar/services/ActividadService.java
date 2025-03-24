package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.dto.ActividadCreateDto;
import com.unicolombo.bienestar.models.Actividad;
import com.unicolombo.bienestar.repositories.ActividadRepository;
import com.unicolombo.bienestar.repositories.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import com.unicolombo.bienestar.models.*;

@Service
public class ActividadService {

    @Autowired
    private ActividadRepository actividadRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public Actividad crearActividad(ActividadCreateDto dto, String adminEmail) {

        //admin
        Usuario admin = usuarioRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if(!"ADMIN".equals(admin.getRol())){
            throw new AccessDeniedException("Solo los administradores pueden crear actividades");
        }

        //instructor
        Usuario instructor = usuarioRepository.findById(dto.getInstructorId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!"INSTRUCTOR".equals(instructor.getRol())) {
            throw new RuntimeException("El usuario asignado no es un instructor");
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

}
