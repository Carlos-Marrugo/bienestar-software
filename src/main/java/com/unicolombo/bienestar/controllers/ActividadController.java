package com.unicolombo.bienestar.controllers;

import com.unicolombo.bienestar.dto.Actividad.ActividadDisponibleDto;
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
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
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
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (bindingResult.hasErrors()) {
            Map<String, String> errores = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            fieldError -> fieldError.getDefaultMessage() != null ?
                                    fieldError.getDefaultMessage() : "Error de validación"));

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "status", "error",
                            "message", "Errores de validación",
                            "errors", errores));
        }

        try {
            Actividad actividad = actividadService.crearActividad(dto, userDetails.getUsername());

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "success");
            response.put("message", "Actividad creada exitosamente");
            response.put("data", mapToDto(actividad));

            if (!actividad.getWarnings().isEmpty()) {
                response.put("warnings", actividad.getWarnings());
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage(),
                    "timestamp", LocalDateTime.now()
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

        if (actividad.getUbicacion() != null) {
            Map<String, Object> ubicacionMap = new LinkedHashMap<>();
            ubicacionMap.put("id", actividad.getUbicacion().getId());
            ubicacionMap.put("nombre", actividad.getUbicacion().getNombre());
            ubicacionMap.put("capacidad", actividad.getUbicacion().getCapacidad());
            dto.put("ubicacion", ubicacionMap);
        }

        dto.put("fechaInicio", actividad.getFechaInicio() != null ? actividad.getFechaInicio().toString() : null);
        dto.put("fechaFin", actividad.getFechaFin() != null ? actividad.getFechaFin().toString() : null);
        dto.put("maxEstudiantes", actividad.getMaxEstudiantes());

        if (actividad.getHorariosEspecificos() != null && !actividad.getHorariosEspecificos().isEmpty()) {
            List<Map<String, Object>> horariosList = actividad.getHorariosEspecificos().stream()
                    .map(h -> {
                        Map<String, Object> horarioMap = new LinkedHashMap<>();
                        horarioMap.put("id", h.getId());
                        horarioMap.put("horaInicio", h.getHoraInicio().toString());
                        horarioMap.put("horaFin", h.getHoraFin().toString());

                        if (h.getHorarioBase() != null) {
                            Map<String, Object> baseMap = new LinkedHashMap<>();
                            baseMap.put("dia", h.getHorarioBase().getDia().name());
                            baseMap.put("horaInicioBase", h.getHorarioBase().getHoraInicio().toString());
                            baseMap.put("horaFinBase", h.getHorarioBase().getHoraFin().toString());
                            horarioMap.put("horarioBase", baseMap);
                        }

                        return horarioMap;
                    })
                    .collect(Collectors.toList());
            dto.put("horarios", horariosList);
        }

        Map<String, Object> instructorMap = new LinkedHashMap<>();
        if (actividad.getInstructor() != null) {
            Instructor instructor = actividad.getInstructor();
            instructorMap.put("id", instructor.getId());
            instructorMap.put("especialidad", instructor.getEspecialidad());
            instructorMap.put("fechaContratacion", instructor.getFechaContratacion());
            instructorMap.put("nombreCompleto", instructor.getNombreCompleto());

            if (instructor.getUsuario() != null) {
                Usuario usuarioInstructor = instructor.getUsuario();
                Map<String, Object> usuarioMap = new LinkedHashMap<>();
                usuarioMap.put("id", usuarioInstructor.getId());
                usuarioMap.put("nombre", usuarioInstructor.getNombre());
                usuarioMap.put("apellido", usuarioInstructor.getApellido());
                usuarioMap.put("email", usuarioInstructor.getEmail());
                usuarioMap.put("rol", usuarioInstructor.getRol() != null ? usuarioInstructor.getRol().name() : null);
                usuarioMap.put("activo", usuarioInstructor.isActivo());
                instructorMap.put("usuario", usuarioMap);
            }
        }
        dto.put("instructor", instructorMap);

        return dto;
    }

    private Map<String, Object> mapToDto(Actividad actividad) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", actividad.getId());
        dto.put("nombre", actividad.getNombre());
        dto.put("fechaInicio", actividad.getFechaInicio());
        dto.put("fechaFin", actividad.getFechaFin());
        dto.put("maxEstudiantes", actividad.getMaxEstudiantes());

        if (actividad.getUbicacion() != null) {
            Map<String, Object> ubicacionMap = new LinkedHashMap<>();
            ubicacionMap.put("id", actividad.getUbicacion().getId());
            ubicacionMap.put("nombre", actividad.getUbicacion().getNombre());
            dto.put("ubicacion", ubicacionMap);
        }

        if (actividad.getInstructor() != null && actividad.getInstructor().getUsuario() != null) {
            Map<String, Object> instructorMap = new LinkedHashMap<>();
            instructorMap.put("id", actividad.getInstructor().getId());
            instructorMap.put("nombre", actividad.getInstructor().getUsuario().getNombre());
            dto.put("instructor", instructorMap);
        }

        if (actividad.getHorariosEspecificos() != null) {
            List<Map<String, Object>> horariosList = actividad.getHorariosEspecificos().stream()
                    .map(h -> {
                        Map<String, Object> horarioMap = new LinkedHashMap<>();
                        horarioMap.put("id", h.getId());
                        horarioMap.put("horaInicio", h.getHoraInicio());
                        horarioMap.put("horaFin", h.getHoraFin());

                        if (h.getHorarioBase() != null) {
                            Map<String, Object> baseMap = new LinkedHashMap<>();
                            baseMap.put("id", h.getHorarioBase().getId());
                            baseMap.put("dia", h.getHorarioBase().getDia());
                            baseMap.put("horaInicioBase", h.getHorarioBase().getHoraInicio());
                            baseMap.put("horaFinBase", h.getHorarioBase().getHoraFin());
                            horarioMap.put("horarioBase", baseMap);
                        }

                        return horarioMap;
                    })
                    .collect(Collectors.toList());
            dto.put("horarios", horariosList);
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

    @Operation(summary = "Listar actividades disponibles para estudiantes",
            description = "Muestra un listado simplificado de actividades disponibles con sus horarios y creador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado exitoso"),
            @ApiResponse(responseCode = "500", description = "Error al procesar la solicitud")
    })
    @GetMapping("/estudiantes/actividades/disponibles")
    public ResponseEntity<?> listarActividadesDisponibles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false, defaultValue = "ASC") String direction) {

        try {
            log.info("Solicitando lista de actividades disponibles, página: {}, tamaño: {}", page, size);

            Sort.Direction sortDirection = direction.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sort = orderBy != null ?
                    Sort.by(sortDirection, orderBy) :
                    Sort.by(sortDirection, "fechaInicio");

            Pageable pageable = PageRequest.of(page, size, sort);

            Page<ActividadDisponibleDto> actividades = actividadService.obtenerActividadesDisponiblesParaEstudiantes(pageable);

            log.info("Se encontraron {} actividades disponibles", actividades.getTotalElements());

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", actividades.getContent());
            response.put("meta", Map.of(
                    "page", actividades.getNumber(),
                    "size", actividades.getSize(),
                    "totalElements", actividades.getTotalElements(),
                    "totalPages", actividades.getTotalPages(),
                    "first", actividades.isFirst(),
                    "last", actividades.isLast()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error inesperado al listar actividades disponibles", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "Error al procesar la solicitud: " + e.getMessage()
            ));
        }
    }
}