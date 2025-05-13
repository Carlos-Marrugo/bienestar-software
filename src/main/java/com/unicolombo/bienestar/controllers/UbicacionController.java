package com.unicolombo.bienestar.controllers;

import com.unicolombo.bienestar.dto.Actividad.UbicacionDto;
import com.unicolombo.bienestar.exceptions.BusinessException;
import com.unicolombo.bienestar.models.Ubicacion;
import com.unicolombo.bienestar.services.UbicacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Map;

@RestController
@RequestMapping("/api/ubicaciones")
@Tag(name = "Ubicaciones", description = "Gestión de ubicaciones para actividades")
@RequiredArgsConstructor
public class UbicacionController {
    private final UbicacionService ubicacionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear nueva ubicación")
    public ResponseEntity<?> crearUbicacion(
            @Valid @RequestBody UbicacionDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        Ubicacion ubicacion = ubicacionService.crearUbicacion(dto, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("status", "success", "data", ubicacion));
    }

    @GetMapping
    @Operation(summary = "Listar ubicaciones activas")
    public ResponseEntity<?> listarUbicaciones() {
        return ResponseEntity.ok(Map.of("data", ubicacionService.listarUbicacionesActivas()));
    }


    @GetMapping("/{id}/disponibilidad")
    public ResponseEntity<?> verificarDisponibilidad(
            @PathVariable Long id,
            @RequestParam DayOfWeek dia,
            @RequestParam String horaInicio,
            @RequestParam String horaFin) {

        try {
            LocalTime inicio = LocalTime.parse(horaInicio);
            LocalTime fin = LocalTime.parse(horaFin);

            boolean disponible = ubicacionService.verificarDisponibilidad(id, dia, inicio, fin);

            return ResponseEntity.ok(Map.of(
                    "disponible", disponible,
                    "mensaje", disponible ? "Disponible" : "No disponible"
            ));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "disponible", false,
                    "mensaje", e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener ubicación por ID")
    public ResponseEntity<?> obtenerUbicacion(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("data", ubicacionService.obtenerUbicacion(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar ubicación")
    public ResponseEntity<?> actualizarUbicacion(
            @PathVariable Long id,
            @Valid @RequestBody UbicacionDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        Ubicacion ubicacion = ubicacionService.actualizarUbicacion(id, dto, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("status", "success", "data", ubicacion));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desactivar ubicación")
    public ResponseEntity<?> desactivarUbicacion(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        ubicacionService.desactivarUbicacion(id, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("status", "success", "message", "Ubicación desactivada"));
    }
}