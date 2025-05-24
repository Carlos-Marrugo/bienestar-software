package com.unicolombo.bienestar.controllers;

import com.unicolombo.bienestar.dto.request.estudiante.RegistroAsistenciaDto;
import com.unicolombo.bienestar.dto.request.estudiante.RegistroHorasDto;
import com.unicolombo.bienestar.exceptions.BusinessException;
import com.unicolombo.bienestar.models.Asistencia;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/instructores")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Registro de Horas", description = "Gestión de asistencias y registro de horas")
public class RegistroHorasController {

    private final RegistroHorasService registroHorasService;

    @Operation(summary = "Registrar asistencia",
            description = "Permite a un instructor registrar la asistencia de un estudiante a una actividad")
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
            log.error("Error al registrar asistencia: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error inesperado al registrar asistencia: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Error interno del servidor"
            ));
        }
    }

    @Operation(summary = "Obtener asistencias del estudiante",
            description = "Obtiene todas las asistencias registradas de un estudiante en una actividad específica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de asistencias obtenido"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @GetMapping("/actividades/{actividadId}/estudiantes/{estudianteId}/verificar-asistencia")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> obtenerAsistenciasEstudiante(
            @PathVariable Long actividadId,
            @PathVariable Long estudianteId,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            List<Asistencia> asistencias = registroHorasService.obtenerAsistenciasEstudiante(
                    actividadId, estudianteId, userDetails.getUsername());

            // Verificar si hay asistencias para el día actual
            Optional<Asistencia> asistenciaHoy = asistencias.stream()
                    .filter(a -> a.getFecha().equals(LocalDate.now()))
                    .findFirst();

            // Convertir las asistencias a un formato más legible para la respuesta
            List<Map<String, ?>> asistenciasResponse = asistencias.stream()
                    .map(a -> Map.of(
                            "id", a.getId(),
                            "fecha", a.getFecha(),
                            "horas", a.getHoras(),
                            "confirmada", a.isConfirmada(),
                            "fechaRegistro", a.getFechaRegistro()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "inscrito", true,
                    "tieneAsistenciaHoy", asistenciaHoy.isPresent(),
                    "asistenciaIdHoy", asistenciaHoy.map(Asistencia::getId).orElse(null),
                    "asistencias", asistenciasResponse
            ));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error inesperado al obtener asistencias: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Error interno del servidor"
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
        } catch (Exception e) {
            log.error("Error inesperado al registrar horas: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Error interno del servidor"
            ));
        }
    }

    @Operation(summary = "Verificar inscripción",
            description = "Verifica si un estudiante está inscrito en una actividad")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificación realizada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
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
        } catch (Exception e) {
            log.error("Error inesperado al verificar inscripción: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Error interno del servidor"
            ));
        }
    }
}