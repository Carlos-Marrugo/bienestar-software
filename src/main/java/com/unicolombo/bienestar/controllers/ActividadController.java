package com.unicolombo.bienestar.controllers;

import com.unicolombo.bienestar.dto.ActividadCreateDto;
import com.unicolombo.bienestar.exceptions.BusinessException;
import com.unicolombo.bienestar.exceptions.ErrorResponse;
import com.unicolombo.bienestar.models.*;
import com.unicolombo.bienestar.repositories.ActividadRepository;
import com.unicolombo.bienestar.repositories.UsuarioRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ActividadRepository actividadRepository;

    //swagger
    @Operation(summary = "Crear nueva actividad", description = "Requiere rol ADMIN")


    @PostMapping("/nueva")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crearActividad(
            @Valid @RequestBody ActividadCreateDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Creando actividad por admin: {}", userDetails.getUsername());

        Actividad actividad = actividadService.crearActividad(dto, userDetails.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "status", "success",
                        "message", "Actividad creada exitosamente",
                        "data", actividad,
                        "timestamp", LocalDateTime.now()
                )
        );
    }

    @Operation(summary = "Obtener actividad por ID",
            description = "ADMIN: Acceso total | INSTRUCTOR: Solo sus actividades | ESTUDIANTE: Solo actividades inscritas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actividad encontrada"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Actividad no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getActividadById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Actividad no encontrada"));

        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        if (usuario.getRol() == Role.INSTRUCTOR &&
                !actividad.getInstructor().getId().equals(usuario.getId())) {
            throw new AccessDeniedException("No tienes permisos para ver esta actividad");
        }

        /*if (usuario.getRol() == Role.ESTUDIANTE &&
                actividad.getInscripciones().stream()
                        .noneMatch(i -> i.getEstudiante().getUsuario().getId().equals(usuario.getId()))) {
            throw new AccessDeniedException("No estás inscrito en esta actividad");
        }*/

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", mapearActividadDto(actividad)
        ));
    }

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

    @Operation(summary = "Listar actividades del instructor", description = "Requiere rol INSTRUCTOR o ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado exitoso"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Instructor no encontrado")
    })
    @GetMapping("/instructor/{instructorId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('INSTRUCTOR') and #instructorId == principal.instructor.id)")
    public ResponseEntity<?> getActividadesPorInstructor(
            @PathVariable Long instructorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

            if (usuario.getRol() == Role.INSTRUCTOR &&
                    !usuario.getId().equals(instructorId)) {
                throw new AccessDeniedException("Solo puedes ver tus propias actividades");
            }

            Page<Actividad> actividades = actividadService.findByInstructorId(instructorId, page, size);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", actividades.getContent().stream().map(this::mapearActividadDto),
                    "pagination", Map.of(
                            "currentPage", actividades.getNumber(),
                            "totalItems", actividades.getTotalElements(),
                            "totalPages", actividades.getTotalPages()
                    )
            ));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "Error al listar actividades del instructor"
            ));
        }
    }

    private Map<String, Object> mapearActividadDto(Actividad actividad) {
        Instructor instructor = actividad.getInstructor();
        Usuario usuarioInstructor = actividad.getInstructor().getUsuario();

        Map<String,Object> instructorMap = new LinkedHashMap<>();
        instructorMap.put("id", instructor.getId());
        instructorMap.put("nombre", Map.of(
                "id", instructor.getId(),
                "usuario", Map.of(
                        "id", usuarioInstructor.getId(),
                        "nombre", usuarioInstructor.getNombre(),
                        "apellido", usuarioInstructor.getApellido(),
                        "email", usuarioInstructor.getEmail(),
                        "rol", usuarioInstructor.getRol().name(),
                        "activo", usuarioInstructor.isActivo()

                ),
                "especialidad", instructor.getEspecialidad(),
                "fechaContratacion", instructor.getFechaContratacion(),
                "nombreCompleto", instructor.getNombreCompleto()

        ));

        return Map.of(
                "id", actividad.getId(),
                "nombre", actividad.getNombre(),
                "ubicacion", actividad.getUbicacion(),
                "fechaInicio", actividad.getFechaInicio(),
                "fechaFin", actividad.getFechaFin(),
                "horaInicio", actividad.getHoraInicio(),
                "horaFin", actividad.getHoraFin(),
                "maxEstudiantes", actividad.getMaxEstudiantes(),
                "instructor", instructorMap
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