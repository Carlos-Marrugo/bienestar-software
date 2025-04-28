package com.unicolombo.bienestar.controllers;

import com.unicolombo.bienestar.dto.ActividadCreateDto;
import com.unicolombo.bienestar.exceptions.BusinessException;
import com.unicolombo.bienestar.exceptions.ErrorResponse;
import com.unicolombo.bienestar.models.Actividad;
import com.unicolombo.bienestar.services.ActividadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.time.*;


import java.util.*;


@RestController
@RequestMapping("/api/actividades")
@Slf4j
@Tag(name = "Actividades", description = "Gestión de actividades deportivas y académicas")

public class ActividadController {

    @Autowired
    private ActividadService actividadService;

    //swagger
    @Operation(summary = "Crear nueva actividad", description = "Requiere rol ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Actividad creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "409", description = "Conflicto (horario ocupado)")
    })


    @GetMapping("/creadas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listarActividades(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String filtro,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            Page<Actividad> actividades = actividadService.listarActividadesAdmin(page, size, filtro);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", actividades.getContent().stream().map(this::mapearActividadDto),
                    "pagination", Map.of(
                            "currentPage", actividades.getNumber(),
                            "totalItems", actividades.getTotalElements(),
                            "totalPages", actividades.getTotalPages()
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "Error al listar actividades",
                    "error", e.getMessage()
            ));
        }
    }

    private Map<String, Object> mapearActividadDto(Actividad actividad) {
        return Map.of(
                "id", actividad.getId(),
                "nombre", actividad.getNombre(),
                "ubicacion", actividad.getUbicacion(),
                "fechaInicio", actividad.getFechaInicio(),
                "fechaFin", actividad.getFechaFin(),
                "horaInicio", actividad.getHoraInicio(),
                "horaFin", actividad.getHoraFin(),
                "maxEstudiantes", actividad.getMaxEstudiantes(),
                //"inscritos", actividad.getInscripciones().size(),
                "instructor", Map.of(
                        "id", actividad.getInstructor().getId(),
                        "nombre", actividad.getInstructor()
                )
                //"creadoEn", actividad.getCreatedAt()
        );
    }


    @PostMapping("/nueva")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crearActividad(
            @Valid @RequestBody ActividadCreateDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Creando actividad por admin: {}", userDetails.getUsername());

        Actividad actividad = actividadService.crearActividad(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "status", "success",
                        "message", "Actividad creada exitosamente",
                        "data", actividad,
                        "timestamp", LocalDateTime.now()
                )
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> editarActividad(
            @PathVariable Long id,
            @Valid @RequestBody ActividadCreateDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            Actividad actividadActualizada = actividadService.editarActividad(id, dto);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Actividad actualizada exitosamente",
                    "data", actividadActualizada
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", "error",
                    "message", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> eliminarActividad(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            actividadService.eliminarActividad(id);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Actividad eliminada exitosamente",
                    "id", id
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", "error",
                    "message", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }
}