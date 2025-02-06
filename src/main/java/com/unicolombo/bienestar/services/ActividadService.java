package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.exceptions.ActividadNoDisponibleException;
import com.unicolombo.bienestar.models.Actividad;
import com.unicolombo.bienestar.repositories.ActividadRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

public class ActividadService {

    @Autowired
    private ActividadRepository actividadRepository;

    public List<Actividad> getAllActividades() {
        return actividadRepository.findAll();
    }

    public Actividad getActividadById(Long id) {
        return actividadRepository.findById(id)
                .orElseThrow(() -> new ActividadNoDisponibleException("Actividad no encontrada"));
    }

    public Actividad createActividad(Actividad actividad) {
        List<Actividad> actividadesInstructor = actividadRepository.findByInstructorAndFechaInicioBetween(
                actividad.getInstructor(),
                actividad.getFechaInicio(),
                actividad.getFechaFin()
        );

        if(!actividadesInstructor.isEmpty()) {
            throw new RuntimeException("El instructor ya está asignado a otra actividad en este horario.");
        }
        return actividadRepository.save(actividad);
    }

    public Actividad updateActividad(Long id, Actividad actividadDetails) {
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));
        actividad.setNombre(actividadDetails.getNombre());
        actividad.setDescripcion(actividadDetails.getDescripcion());
        actividad.setInstructor(actividadDetails.getInstructor());
        actividad.setCoordinador(actividadDetails.getCoordinador());
        actividad.setLugar(actividadDetails.getLugar());
        actividad.setCupoMaximo(actividadDetails.getCupoMaximo());
        actividad.setFechaInicio(actividadDetails.getFechaInicio());
        actividad.setFechaFin(actividadDetails.getFechaFin());
        actividad.setActiva(actividadDetails.isActiva());
        return actividadRepository.save(actividad);
    }

    public void deleteActividad(Long id) {
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));
        actividadRepository.delete(actividad);
    }
}
