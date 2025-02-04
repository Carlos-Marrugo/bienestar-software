package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.models.Horario;
import com.unicolombo.bienestar.repositories.HorarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HorarioService {

    @Autowired
    private HorarioRepository horarioRepository;

    public List<Horario> getAllHorarios() {
        return horarioRepository.findAll();
    }

    public Optional<Horario> getHorarioById(Long id) {
        return horarioRepository.findById(id);
    }

    public Horario createHorario(Horario horario) {
        return horarioRepository.save(horario);
    }

    public Horario updateHorario(Long id, Horario horarioDetails) {
        Horario horario = horarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado"));
        horario.setActividad(horarioDetails.getActividad());
        horario.setDiaSemana(horarioDetails.getDiaSemana());
        horario.setHoraInicio(horarioDetails.getHoraInicio());
        horario.setHoraFin(horarioDetails.getHoraFin());
        return horarioRepository.save(horario);
    }

    public void deleteHorario(Long id) {
        Horario horario = horarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado"));
        horarioRepository.delete(horario);
    }
}