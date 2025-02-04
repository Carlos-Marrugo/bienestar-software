package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.models.Asistencia;
import com.unicolombo.bienestar.repositories.AsistenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

public class AsistenciaService {

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    public List<Asistencia> getAllAsistencias() {
        return asistenciaRepository.findAll();
    }

    public Optional<Asistencia> getAsistenciaById(Long id) {
        return asistenciaRepository.findById(id);
    }

    public Asistencia createAsistencia(Asistencia asistencia) {
        return asistenciaRepository.save(asistencia);
    }

    public Asistencia updateAsistencia(Long id, Asistencia asistenciaDetails) {
        Asistencia asistencia = asistenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asistencia no encontrada"));
        asistencia.setInscripcion(asistenciaDetails.getInscripcion());
        asistencia.setFecha(asistenciaDetails.getFecha());
        asistencia.setHorasCumplidas(asistenciaDetails.getHorasCumplidas());
        asistencia.setObservaciones(asistenciaDetails.getObservaciones());
        return asistenciaRepository.save(asistencia);
    }

    public void deleteAsistencia(Long id) {
        Asistencia asistencia = asistenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asistencia no encontrada"));
        asistenciaRepository.delete(asistencia);
    }
}
