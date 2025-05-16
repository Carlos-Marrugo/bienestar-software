package com.unicolombo.bienestar.controllers;

import com.unicolombo.bienestar.dto.ActividadCreateDto;
import com.unicolombo.bienestar.exceptions.BusinessException;
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
import java.util.stream.Collectors;

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

    @Operation(summary = "Crear nueva actividad", description = "Requiere rol ADMIN")
    @PostMapping("/nueva")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crearActividad(
            @Valid @RequestBody ActividadCreateDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            Actividad actividad = actividadService.crearActividad(dto, userDetails.getUsername());

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "success");
            response.put("message", "Actividad creada exitosamente");
            response.put("data", mapToDto(actividad));

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }


    @Operation(summary = "Obtener actividad por ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> getActividadById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Actividad actividad = actividadService.findById(id);
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
                "data", mapToDto(actividad)
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
                    "data", actividades.getContent().stream()
                            .map(this::mapearActividadDto)
                            .collect(Collectors.toList()),
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
        Objects.requireNonNull(actividad, "La actividad no puede ser nula");

        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", actividad.getId());
        dto.put("nombre", actividad.getNombre());

        if (actividad.getHorarioUbicacion() != null && actividad.getHorarioUbicacion().getUbicacion() != null) {
            Map<String, Object> ubicacionMap = new LinkedHashMap<>();
            ubicacionMap.put("id", actividad.getHorarioUbicacion().getUbicacion().getId());
            ubicacionMap.put("nombre", actividad.getHorarioUbicacion().getUbicacion().getNombre());
            dto.put("ubicacion", ubicacionMap);
        } else {
            dto.put("ubicacion", null);
        }

        dto.put("fechaInicio", actividad.getFechaInicio() != null ? actividad.getFechaInicio().toString() : null);
        dto.put("fechaFin", actividad.getFechaFin() != null ? actividad.getFechaFin().toString() : null);

        if (actividad.getHorarioUbicacion() != null) {
            dto.put("horaInicio", actividad.getHorarioUbicacion().getHoraInicio().toString());
            dto.put("horaFin", actividad.getHorarioUbicacion().getHoraFin().toString());
        } else {
            dto.put("horaInicio", null);
            dto.put("horaFin", null);
        }

        dto.put("maxEstudiantes", actividad.getMaxEstudiantes());

        Map<String, Object> instructorMap = new LinkedHashMap<>();
        if (actividad.getInstructor() != null && actividad.getInstructor().getUsuario() != null) {
            Instructor instructor = actividad.getInstructor();
            Usuario usuarioInstructor = instructor.getUsuario();

            instructorMap.put("id", instructor.getId());

            Map<String, Object> usuarioMap = new LinkedHashMap<>();
            usuarioMap.put("id", usuarioInstructor.getId());
            usuarioMap.put("nombre", usuarioInstructor.getNombre());
            usuarioMap.put("apellido", usuarioInstructor.getApellido());
            usuarioMap.put("email", usuarioInstructor.getEmail());
            usuarioMap.put("rol", usuarioInstructor.getRol() != null ? usuarioInstructor.getRol().name() : null);
            usuarioMap.put("activo", usuarioInstructor.isActivo());

            instructorMap.put("usuario", usuarioMap);
            instructorMap.put("especialidad", instructor.getEspecialidad());
            instructorMap.put("fechaContratacion", instructor.getFechaContratacion());
            instructorMap.put("nombreCompleto", instructor.getNombreCompleto());
        }
        dto.put("instructor", instructorMap);

        return dto;
    }

    private Map<String, Object> mapToDto(Actividad actividad) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", actividad.getId());
        dto.put("nombre", actividad.getNombre());

        if (actividad.getUbicacion() != null) {
            Map<String, Object> ubicacionMap = new LinkedHashMap<>();
            ubicacionMap.put("id", actividad.getUbicacion().getId());
            ubicacionMap.put("nombre", actividad.getUbicacion().getNombre());
            dto.put("ubicacion", ubicacionMap);
        }

        dto.put("fechaInicio", actividad.getFechaInicio());
        dto.put("fechaFin", actividad.getFechaFin());
        dto.put("maxEstudiantes", actividad.getMaxEstudiantes());

        List<Map<String, Object>> horariosList = new ArrayList<>();
        for (HorarioUbicacion horario : actividad.getHorarios()) {
            Map<String, Object> horarioMap = new LinkedHashMap<>();
            horarioMap.put("id", horario.getId());
            horarioMap.put("dia", horario.getDia().name());
            horarioMap.put("horaInicio", horario.getHoraInicio().toString());
            horarioMap.put("horaFin", horario.getHoraFin().toString());
            horariosList.add(horarioMap);
        }
        dto.put("horarios", horariosList);

        if (actividad.getInstructor() != null) {
            Map<String, Object> instructorMap = new LinkedHashMap<>();
            instructorMap.put("id", actividad.getInstructor().getId());
            if (actividad.getInstructor().getUsuario() != null) {
                instructorMap.put("nombre", actividad.getInstructor().getUsuario().getNombre());
                instructorMap.put("apellido", actividad.getInstructor().getUsuario().getApellido());
            }
            dto.put("instructor", instructorMap);
        }

        return dto;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> editarActividad(
            @PathVariable Long id,
            @Valid @RequestBody ActividadCreateDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            Actividad actividadActualizada = actividadService.editarActividad(id, dto, userDetails.getUsername());
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

    @DeleteMapping("eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> eliminarActividad(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            actividadService.eliminarActividad(id, userDetails.getUsername());
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