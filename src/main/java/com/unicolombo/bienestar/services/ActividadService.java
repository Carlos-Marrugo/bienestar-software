package com.unicolombo.bienestar.services;

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

    public Optional<Actividad> getActividadById(Long id) {
        return actividadRepository.findById(id);
    }

    public Actividad createActividad(Actividad actividad) {
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
