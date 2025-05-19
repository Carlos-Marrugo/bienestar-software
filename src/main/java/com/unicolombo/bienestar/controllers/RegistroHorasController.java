package com.unicolombo.bienestar.controllers;

import com.unicolombo.bienestar.dto.RegistroAsistenciaDto;
import com.unicolombo.bienestar.dto.RegistroHorasDto;
import com.unicolombo.bienestar.exceptions.BusinessException;
import com.unicolombo.bienestar.services.RegistroHorasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/instructores")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Registro de Horas", description = "Gestión de asistencias y registro de horas")
public class RegistroHorasController {

    private final RegistroHorasService registroHorasService;

    @Operation(summary = "Registrar asistencia",
            description = "Permite a un instructor registrar la asistencia de un estudiante")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asistencia registrada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @PostMapping("/asistencias")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> registrarAsistencia(
            @Valid @RequestBody RegistroAsistenciaDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            Long asistenciaId = registroHorasService.registrarAsistencia(dto, userDetails.getUsername());
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Asistencia registrada exitosamente",
                    "asistenciaId", asistenciaId
            ));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    @Operation(summary = "Registrar horas",
            description = "Permite a un instructor registrar horas a un estudiante con asistencia confirmada")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Horas registradas"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @PostMapping("/horas")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> registrarHoras(
            @Valid @RequestBody RegistroHorasDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            registroHorasService.registrarHoras(dto, userDetails.getUsername());
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Horas registradas exitosamente"
            ));
        } catch (BusinessException e) {
            log.error("Error al registrar horas: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/actividades/{actividadId}/estudiantes/{estudianteId}/verificar-inscripcion")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> verificarInscripcion(
            @PathVariable Long actividadId,
            @PathVariable Long estudianteId,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            boolean inscrito = registroHorasService.verificarInscripcion(actividadId, estudianteId, userDetails.getUsername());
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "inscrito", inscrito
            ));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }
}