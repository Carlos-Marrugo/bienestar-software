
package com.unicolombo.bienestar.controllers;

import com.unicolombo.bienestar.dto.Actividad.HorarioUbicacionDto;
import com.unicolombo.bienestar.dto.Actividad.UbicacionDto;
import com.unicolombo.bienestar.exceptions.BusinessException;
import com.unicolombo.bienestar.models.DiaSemana;
import com.unicolombo.bienestar.models.HorarioUbicacion;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        List<Ubicacion> ubicaciones = ubicacionService.listarUbicacionesActivas();

        List<Map<String, Object>> response = ubicaciones.stream().map(u -> {
            Map<String, Object> ubicacionMap = new LinkedHashMap<>();
            ubicacionMap.put("id", u.getId());
            ubicacionMap.put("nombre", u.getNombre());
            ubicacionMap.put("capacidad", u.getCapacidad());
            ubicacionMap.put("activa", u.getActiva());

            if(u.getHorarios() != null) {
                ubicacionMap.put("horarios", u.getHorarios().stream().map(h -> {
                    Map<String, Object> horarioMap = new LinkedHashMap<>();
                    horarioMap.put("id", h.getId());
                    horarioMap.put("dia", h.getDia());
                    horarioMap.put("horaInicio", h.getHoraInicio());
                    horarioMap.put("horaFin", h.getHoraFin());
                    return horarioMap;
                }).collect(Collectors.toList()));
            }

            return ubicacionMap;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", response
        ));
    }

    @Operation(summary = "Obtener horarios en uso de una ubicación")
    @GetMapping("/{id}/horarios-en-uso")
    public ResponseEntity<?> getHorariosEnUso(@PathVariable Long id) {
        try {
            List<HorarioUbicacion> horarios = ubicacionService.findHorariosConActividades(id);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", horarios
            ));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }



    @GetMapping("/{id}/disponibilidad")
    public ResponseEntity<?> verificarDisponibilidad(
            @PathVariable Long id,
            @RequestParam String dia,
            @RequestParam String horaInicio,
            @RequestParam String horaFin,
            @RequestParam(required = false) String fecha) {

        try {
            DiaSemana diaSemana = DiaSemana.valueOf(dia.toUpperCase());
            LocalTime inicio = LocalTime.parse(horaInicio);
            LocalTime fin = LocalTime.parse(horaFin);
            LocalDate fechaConsulta = fecha != null ? LocalDate.parse(fecha) : null;

            boolean disponible = ubicacionService.verificarDisponibilidad(
                    id,
                    diaSemana,
                    inicio,
                    fin,
                    fechaConsulta
            );

            return ResponseEntity.ok(Map.of(
                    "disponible", disponible,
                    "mensaje", disponible ? "Disponible" : "No disponible"
            ));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "disponible", false,
                    "mensaje", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "disponible", false,
                    "mensaje", "Error al verificar disponibilidad: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener ubicación por ID")
    public ResponseEntity<?> obtenerUbicacion(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("data", ubicacionService.obtenerUbicacion(id)));
    }

    @Operation(summary = "Actualizar ubicación")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizarUbicacion(
            @PathVariable Long id,
            @Valid @RequestBody UbicacionDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            Ubicacion ubicacion = ubicacionService.actualizarUbicacion(id, dto, userDetails.getUsername());
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", ubicacion
            ));
        } catch (BusinessException e) {
            if (e.getData() != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "status", "error",
                        "message", e.getMessage(),
                        "horariosEnUso", e.getData()
                ));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    @Operation(summary = "Actualizar horarios de ubicación")
    @PutMapping("/{id}/horarios")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizarHorarios(
            @PathVariable Long id,
            @Valid @RequestBody List<HorarioUbicacionDto> horariosDto,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            Ubicacion ubicacion = ubicacionService.actualizarHorarios(id, horariosDto, userDetails.getUsername());
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Horarios actualizados correctamente",
                    "data", ubicacion
            ));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "status", "error",
                    "message", e.getMessage(),
                    "horariosEnUso", e.getData() != null ? e.getData() : Collections.emptyList()
            ));
        }
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