package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.dto.RegistroHorasDto;
import com.unicolombo.bienestar.exceptions.BusinessException;
import com.unicolombo.bienestar.models.Actividad;
import com.unicolombo.bienestar.models.Estudiante;
import com.unicolombo.bienestar.models.RegistroHoras;
import com.unicolombo.bienestar.repositories.ActividadRepository;
import com.unicolombo.bienestar.repositories.EstudianteRepository;
import com.unicolombo.bienestar.repositories.RegistroHorasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RegistroHorasService {

    private final RegistroHorasRepository registroHorasRepository;
    private final ActividadRepository actividadRepository;
    private final EstudianteRepository estudianteRepository;

    @Autowired
    public RegistroHorasService(
            RegistroHorasRepository registroHorasRepository,
            ActividadRepository actividadRepository,
            EstudianteRepository estudianteRepository) {
        this.registroHorasRepository = registroHorasRepository;
        this.actividadRepository = actividadRepository;
        this.estudianteRepository = estudianteRepository;
    }

    public RegistroHoras registrarHoras(Long actividadId, RegistroHorasDto dto) {
        Actividad actividad = actividadRepository.findById(actividadId)
                .orElseThrow(() -> new BusinessException("Actividad no encontrada"));

        Estudiante estudiante = estudianteRepository.findById(dto.getEstudianteId())
                .orElseThrow(() -> new BusinessException("Estudiante no encontrado"));

        int totalHoras = registroHorasRepository
                .sumarHorasPorEstudianteYActividad(estudiante.getId(), actividad.getId())
                .orElse(0);

        if (totalHoras + dto.getHoras() > 42) {
            throw new BusinessException("El estudiante no puede registrar más de 42 horas en total");
        }

        RegistroHoras registro = new RegistroHoras();
        registro.setActividad(actividad);
        registro.setEstudiante(estudiante);
        registro.setHoras(dto.getHoras());
        registro.setDescripcion(dto.getDescripcion());
        registro.setFechaRegistro(LocalDateTime.now());

        return registroHorasRepository.save(registro);
    }
}
