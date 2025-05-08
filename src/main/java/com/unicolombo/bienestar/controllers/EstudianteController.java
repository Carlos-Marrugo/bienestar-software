package com.unicolombo.bienestar.controllers;

import com.unicolombo.bienestar.dto.RegistroEstudianteDto;
import com.unicolombo.bienestar.models.Estudiante;
import com.unicolombo.bienestar.services.EstudianteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/estudiantes")
@Tag(name = "2. Estudiantes", description = "Gestión de estudiantes y sus registros")
public class EstudianteController {

    @Autowired
    private EstudianteService estudianteService;

    @Operation(
            summary = "Registrar un nuevo estudiante",
            description = "Crea un nuevo estudiante en el sistema"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Estudiante creado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o estudiante ya existente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)
                    )
            )
    })

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