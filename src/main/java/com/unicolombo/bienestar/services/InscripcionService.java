package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.models.Inscripcion;
import com.unicolombo.bienestar.repositories.InscripcionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InscripcionService {

    @Autowired
    private InscripcionRepository inscripcionRepository;

    public List<Inscripcion> getAllInscripciones() {
        return inscripcionRepository.findAll();
    }

    public Optional<Inscripcion> getInscripcionById(Long id) {
        return inscripcionRepository.findById(id);
    }

    public Inscripcion createInscripcion(Inscripcion inscripcion) {
        return inscripcionRepository.save(inscripcion);
    }

    public Inscripcion updateInscripcion(Long id, Inscripcion inscripcionDetails) {
        Inscripcion inscripcion = inscripcionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscripción no encontrada"));
        inscripcion.setEstudiante(inscripcionDetails.getEstudiante());
        inscripcion.setActividad(inscripcionDetails.getActividad());
        inscripcion.setEstado(inscripcionDetails.getEstado());
        return inscripcionRepository.save(inscripcion);
    }

    public void deleteInscripcion(Long id) {
        Inscripcion inscripcion = inscripcionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscripción no encontrada"));
        inscripcionRepository.delete(inscripcion);
    }
}