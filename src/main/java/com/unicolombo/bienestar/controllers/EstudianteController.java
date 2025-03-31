package com.unicolombo.bienestar.controllers;

import com.unicolombo.bienestar.dto.RegistroEstudianteDto;
import com.unicolombo.bienestar.models.Estudiante;
import com.unicolombo.bienestar.services.EstudianteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/estudiantes")
public class EstudianteController {

    @Autowired
    private EstudianteService estudianteService;

    @PostMapping("/registro")
    public ResponseEntity<Map<String, Object>> registrarEstudiante(
            @RequestBody RegistroEstudianteDto dto) {


        Estudiante estudiante = estudianteService.registrarEstudiante(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Estudiante registrado exitosamente",
                "data", Map.of(
                        "usuario_id", estudiante.getUsuario().getId(),
                        "estudiante_id", estudiante.getId(),
                        "codigo", estudiante.getCodigoEstudiantil(),
                        "email", estudiante.getUsuario().getEmail()
                )
        ));
    }
}