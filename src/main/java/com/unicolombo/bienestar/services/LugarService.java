package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.models.Lugar;
import com.unicolombo.bienestar.repositories.LugarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LugarService {

    @Autowired
    private LugarRepository lugarRepository;

    public List<Lugar> getAllLugares() {
        return lugarRepository.findAll();
    }

    public Optional<Lugar> getLugarById(Long id) {
        return lugarRepository.findById(id);
    }

    public Lugar createLugar(Lugar lugar) {
        return lugarRepository.save(lugar);
    }

    public Lugar updateLugar(Long id, Lugar lugarDetails) {
        Lugar lugar = lugarRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lugar no encontrado"));
        lugar.setNombre(lugarDetails.getNombre());
        lugar.setDireccion(lugarDetails.getDireccion());
        lugar.setCapacidad(lugarDetails.getCapacidad());
        lugar.setActivo(lugarDetails.isActivo());
        return lugarRepository.save(lugar);
    }

    public void deleteLugar(Long id) {
        Lugar lugar = lugarRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lugar no encontrado"));
        lugarRepository.delete(lugar);
    }
}