package com.unicolombo.bienestar.controllers;

import com.unicolombo.bienestar.dto.ActividadCreateDto;
import com.unicolombo.bienestar.models.Actividad;
import com.unicolombo.bienestar.services.ActividadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ActividadController {

    @Autowired
    private ActividadService actividadService;


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crearActividad(
            @RequestBody ActividadCreateDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            log.info("Intentando crear actividad por usuario: {}", userDetails.getUsername());
            log.info("Datos recibidos: {}", dto);

            Actividad actividad = actividadService.crearActividad(dto);
            return ResponseEntity.ok(actividad);

        } catch (RuntimeException e) {
            log.error("Error al crear actividad: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "error", "No tienes permiso para realizar esta acción",
                            "message", e.getMessage(),
                            "timestamp", LocalDateTime.now()
                    ));
        }
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