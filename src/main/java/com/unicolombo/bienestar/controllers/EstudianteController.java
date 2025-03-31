package com.unicolombo.bienestar.controllers;

import com.unicolombo.bienestar.dto.RegistroEstudianteDto;
import com.unicolombo.bienestar.services.EstudianteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/estudiantes")
public class EstudianteController {

    @Autowired
    private EstudianteService estudianteService;

    @PostMapping("/registro")
    public ResponseEntity<?> registrarEstudiante(@RequestBody RegistroEstudianteDto dto) {
        estudianteService.registrarEstudiante(dto);
        return ResponseEntity.ok().build();
    }
}