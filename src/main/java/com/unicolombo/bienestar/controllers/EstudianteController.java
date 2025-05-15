package com.unicolombo.bienestar.controllers;

import com.unicolombo.bienestar.dto.RegistroEstudianteDto;
import com.unicolombo.bienestar.exceptions.ActividadLlenaException;
import com.unicolombo.bienestar.exceptions.ResourceNotFoundException;
import com.unicolombo.bienestar.exceptions.UnauthorizedException;
import com.unicolombo.bienestar.models.Estudiante;
import com.unicolombo.bienestar.services.EstudianteService;
import com.unicolombo.bienestar.services.InscripcionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/estudiantes")
@Tag(name = "2. Estudiantes", description = "Gestión de estudiantes y sus registros")
public class EstudianteController {

    private final EstudianteService estudianteService;
    private final InscripcionService inscripcionService;

    @Autowired
    public EstudianteController(EstudianteService estudianteService, InscripcionService inscripcionService) {
        this.estudianteService = estudianteService;
        this.inscripcionService = inscripcionService;
    }

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

        if (estudianteService.existeEmail(dto.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "El email ya está registrado"
            ));
        }

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

    @Operation(
            summary = "Inscribir estudiante en una actividad",
            description = "Permite que un estudiante se inscriba a una actividad si hay cupos disponibles"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Inscripción exitosa",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Error en la solicitud"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @PostMapping("/{estudianteId}/inscribirse/{actividadId}")
    public ResponseEntity<Map<String, Object>> inscribirEstudiante(
            @PathVariable Long estudianteId,
            @PathVariable Long actividadId) {

        try {
            // 1. Verificar autenticación
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            // 2. Buscar estudiante
            Estudiante estudiante = estudianteService.obtenerEstudiantePorId(estudianteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado"));

            // 3. Validar que el estudiante coincide con el usuario autenticado
            if (!estudiante.getUsuario().getEmail().equals(username)) {
                throw new UnauthorizedException("No puedes inscribir a otro estudiante");
            }

            // 4. Procesar inscripción
            inscripcionService.inscribirEstudianteEnActividad(estudianteId, actividadId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Inscripción exitosa",
                    "data", Map.of(
                            "estudianteId", estudianteId,
                            "actividadId", actividadId
                    )
            ));

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (ActividadLlenaException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor"
            ));
        }
    }
}