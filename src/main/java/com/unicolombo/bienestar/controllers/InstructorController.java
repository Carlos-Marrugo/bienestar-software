package com.unicolombo.bienestar.controllers;

import com.unicolombo.bienestar.dto.request.instructor.*;
import com.unicolombo.bienestar.dto.request.actividad.ActividadInstructorDto;
import com.unicolombo.bienestar.dto.request.estudiante.EstudianteInscritoDto;
import com.unicolombo.bienestar.dto.response.PageResponse;
import com.unicolombo.bienestar.models.Instructor;
import com.unicolombo.bienestar.repositories.InstructorRepository;
import com.unicolombo.bienestar.services.ActividadService;
import com.unicolombo.bienestar.services.InscripcionService;
import com.unicolombo.bienestar.services.InstructorService;
import com.unicolombo.bienestar.exceptions.BusinessException;
import com.unicolombo.bienestar.services.JwtService;
import com.unicolombo.bienestar.utils.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Slf4j
public class InstructorController {

    @Autowired
    private InstructorService instructorService;

    @Autowired
    private InscripcionService inscripcionService;

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ActividadService actividadService;


    @PostMapping("/agregar-instructor")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registrarInstructor(
            @Valid @RequestBody RegistroInstructorDto dto,
            BindingResult result) {

        if (result.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(getFirstError(result), result));
        }

        try {
            Instructor instructor = instructorService.registrarInstructor(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ResponseWrapper.success(instructor, "Instructor registrado exitosamente"));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(e.getMessage()));
        }
    }

    @GetMapping("/instructores-activos")
    @Operation(summary = "Listar instructores activos",
            description = "Obtiene un listado paginado de instructores activos con opción de búsqueda")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado exitoso"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> listarInstructores(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) String search) {

        try {
            Page<InstructorListDto> resultado = instructorService.listarInstructoresActivos(pageable, search);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", resultado.getContent());
            response.put("pagination", Map.of(
                    "currentPage", resultado.getNumber(),
                    "totalItems", resultado.getTotalElements(),
                    "totalPages", resultado.getTotalPages()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "Error al listar instructores: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> obtenerInstructor(@PathVariable Long id) {
        InstructorDetailDto instructor =  instructorService.obtenerInstructorDetalle(id);
        return ResponseEntity.ok(instructor);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizarInstructorAdmin(
            @PathVariable Long id,
            @Valid @RequestBody InstructorAdminUpdateDto dto,
            BindingResult result) {

        if (result.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(getFirstError(result)));
        }

        try {
            Instructor actualizado = instructorService.actualizarInstructorAdmin(id, dto);
            return ResponseEntity.ok()
                    .body(ResponseWrapper.success(actualizado, "Instructor actualizado"));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> desactivarInstructor(@PathVariable Long id) {
        instructorService.desactivarInstructor(id);
        return ResponseEntity.ok(Map.of("mensaje", "Instructor desactivado correctamente"));
    }

    @Operation(summary = "Obtener perfil del instructor")
    @GetMapping("/perfil/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> obtenerPerfilInstructor(@PathVariable Long id) {
            InstructorPerfilDto perfil = instructorService.obtenerPerfilInstructor(id);
            return ResponseEntity.ok()
                    .body(ResponseWrapper.success(perfil, "Perfil del instructor obtenido"));
    }


    @Operation(summary = "Obtener mis actividades")
    @GetMapping("/mis-actividades")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getMisActividades(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long instructorId = instructorService.getInstructorIdByEmail(userDetails.getUsername());
        Page<ActividadInstructorDto> actividades = instructorService.getActividadesAsignadas(instructorId,pageable , search);
        return ResponseEntity.ok(new PageResponse<>(actividades));
    }

    @Operation(summary = "Obtener mi perfil")
    @GetMapping("/mi-perfil")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> obtenerMiPerfil(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long instructorId = instructorService.getInstructorIdByEmail(userDetails.getUsername());
            Instructor instructor = instructorRepository.findActiveById(instructorId)
                    .orElseThrow(() -> new BusinessException("Instructor no encontrado"));

            Map<String, Object> perfil = new HashMap<>();
            perfil.put("id", instructor.getId());
            perfil.put("nombre", instructor.getUsuario().getNombre());
            perfil.put("apellido", instructor.getUsuario().getApellido());
            perfil.put("email", instructor.getUsuario().getEmail());
            perfil.put("especialidad", instructor.getEspecialidad());
            perfil.put("fechaContratacion", instructor.getFechaContratacion());

            return ResponseEntity.ok()
                    .body(ResponseWrapper.success(perfil, "Mi perfil obtenido"));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(e.getMessage()));
        }
    }

    @Operation(summary = "Actualizar mi perfil")
    @PutMapping("/mi-perfil")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> actualizarMiPerfil(
            @Valid @RequestBody InstructorSelfUpdateDto dto,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (result.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(getFirstError(result)));
        }

        try {
            Long instructorId = instructorService.getInstructorIdByEmail(userDetails.getUsername());
            Instructor actualizado = instructorService.actualizarInstructorSelf(
                    instructorId, dto, userDetails.getUsername());

            return ResponseEntity.ok()
                    .body(ResponseWrapper.success(actualizado, "Perfil actualizado exitosamente"));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(e.getMessage()));
        }
    }


    private String getFirstError(BindingResult result) {
        return result.getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Error de validación");
    }

    @Operation(summary = "Obtener estudiantes inscritos en actividad")
    @GetMapping("/mis-actividades/{actividadId}/estudiantes")
    public ResponseEntity<?> getEstudiantesInscritos(
            @PathVariable Long actividadId,
            @RequestParam(required = false) String filtro,
            @PageableDefault(size = 10)  Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {

            Long instructorId = null;
            String authority = userDetails.getAuthorities().toString();
            if (authority.equals("[ROLE_INSTRUCTOR]")) {
                instructorId = instructorService.getInstructorIdByEmail(userDetails.getUsername());
            }

            Page<EstudianteInscritoDto> resultado = actividadService.getEstudiantesInscritosEnActividad(
                    actividadId,
                    instructorId,
                    filtro,
                    pageable
            );
            return ResponseEntity.ok(new PageResponse<>(resultado));
    }
}