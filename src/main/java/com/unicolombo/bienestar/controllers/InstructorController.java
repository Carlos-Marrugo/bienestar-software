package com.unicolombo.bienestar.controllers;

import com.unicolombo.bienestar.dto.*;
import com.unicolombo.bienestar.dto.Actividad.ActividadInstructorDto;
import com.unicolombo.bienestar.dto.estudiante.EstudianteDto;
import com.unicolombo.bienestar.dto.estudiante.EstudianteInscritoDto;
import com.unicolombo.bienestar.models.Actividad;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listarInstructores() {
        List<InstructorListDto> instructores = instructorService.listarInstructoresActivos();
        return ResponseEntity.ok()
                .body(ResponseWrapper.success(instructores, "Lista de instructores activos"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> obtenerInstructor(@PathVariable Long id) {
        return instructorService.obtenerInstructorDetalle(id)
                .map(instructor -> ResponseEntity.ok()
                        .body(ResponseWrapper.success(instructor, "Instructor encontrado")))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ResponseWrapper.error("Instructor no encontrado")));
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
        try {
            instructorService.desactivarInstructor(id);
            return ResponseEntity.ok()
                    .body(ResponseWrapper.success(null, "Instructor desactivado exitosamente"));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(e.getMessage()));
        }
    }


    @Operation(summary = "Obtener perfil del instructor")
    @GetMapping("/perfil/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> obtenerPerfilInstructor(@PathVariable Long id) {
        try {
            InstructorPerfilDto perfil = instructorService.obtenerPerfilInstructor(id);
            return ResponseEntity.ok()
                    .body(ResponseWrapper.success(perfil, "Perfil del instructor obtenido"));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(e.getMessage()));
        }
    }


    @Operation(summary = "Obtener mis actividades")
    @GetMapping("/mis-actividades")
    public ResponseEntity<?> getMisActividades(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long instructorId = instructorService.getInstructorIdByEmail(userDetails.getUsername());
            List<ActividadInstructorDto> actividades = instructorService.getActividadesAsignadasFormateadas(instructorId);
            return ResponseEntity.ok()
                    .body(ResponseWrapper.success(actividades, "Mis actividades obtenidas"));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(e.getMessage()));
        }
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            Long instructorId = instructorService.getInstructorIdByEmail(userDetails.getUsername());
            Page<EstudianteInscritoDto> resultado = actividadService.getEstudiantesInscritosEnActividad(
                    actividadId,
                    instructorId,
                    filtro,
                    PageRequest.of(page, size, Sort.by("fechaInscripcion").descending()));

            Map<String, Object> response = new HashMap<>();
            response.put("estudiantes", resultado.getContent());
            response.put("paginacion", Map.of(
                    "total", resultado.getTotalElements(),
                    "paginas", resultado.getTotalPages(),
                    "actual", resultado.getNumber()
            ));

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getStatus()).body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }
}