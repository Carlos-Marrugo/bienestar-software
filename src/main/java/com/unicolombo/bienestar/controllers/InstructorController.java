package com.unicolombo.bienestar.controllers;

import com.unicolombo.bienestar.dto.Actividad.ActividadInstructorDto;
import com.unicolombo.bienestar.dto.InstructorUpdateDto;
import com.unicolombo.bienestar.dto.RegistrarHorasDto;
import com.unicolombo.bienestar.dto.RegistroInstructorDto;
import com.unicolombo.bienestar.models.Actividad;
import com.unicolombo.bienestar.models.Instructor;
import com.unicolombo.bienestar.repositories.InstructorRepository;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
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
    public InstructorController(InstructorService instructorService) {
        this.instructorService = instructorService;
    }

    @PostMapping("/agregar-instructor")
    public ResponseEntity<?> registrarInstructor(
            @Valid @RequestBody RegistroInstructorDto dto,
            BindingResult result) {

        if (result.hasErrors()) {
            String errorMessage = !result.getFieldErrors().isEmpty()
                    ? result.getFieldErrors().get(0).getDefaultMessage()
                    : "Error de validación";
            return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(errorMessage, result));
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
    public ResponseEntity<?> listarInstructores() {
        List<Instructor> instructores = instructorService.listarInstructoresActivos();
        return ResponseEntity.ok()
                .body(ResponseWrapper.success(instructores, "Lista de instructores activos"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerInstructor(@PathVariable Long id) {
        Optional<Instructor> instructor = instructorService.obtenerInstructorActivo(id);
        return instructor.map(value -> ResponseEntity.ok()
                        .body(ResponseWrapper.success(value, "Instructor encontrado")))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ResponseWrapper.error("Instructor no encontrado")));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarInstructor(
            @PathVariable Long id,
            @Valid @RequestBody InstructorUpdateDto dto,
            BindingResult result) {

        if (result.hasErrors()) {
            String errorMessage = !result.getFieldErrors().isEmpty()
                    ? result.getFieldErrors().get(0).getDefaultMessage()
                    : "Error de validación";
            return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(errorMessage, result));
        }

        try {
            Instructor actualizado = instructorService.actualizarInstructor(id, dto);
            return ResponseEntity.ok()
                    .body(ResponseWrapper.success(actualizado, "Instructor actualizado"));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
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

    @Operation(summary = "Obtener actividades de un instructor",
            description = "Devuelve las actividades asignadas a un instructor específico (para administradores)")
    @GetMapping("/instructores/{id}/actividades")
    public ResponseEntity<?> getActividadesInstructor(@PathVariable Long id) {
        try {
            List<Actividad> actividades = instructorService.getActividadesAsignadasRaw(id);
            return ResponseEntity.ok()
                    .body(ResponseWrapper.success(actividades, "Actividades del instructor obtenidas"));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(e.getMessage()));
        }
    }

    @Operation(summary = "Obtener mis actividades (para instructores)",
            description = "Devuelve las actividades asignadas al instructor actual con sus horarios específicos")
    @GetMapping("/mis-actividades")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> getMisActividades(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // Eliminar "Bearer "
            String email = jwtService.extractUsername(token);
            Long instructorId = instructorService.getInstructorIdByEmail(email);

            List<ActividadInstructorDto> actividades = instructorService.getActividadesAsignadasFormateadas(instructorId);
            return ResponseEntity.ok()
                    .body(ResponseWrapper.success(actividades, "Mis actividades obtenidas"));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(e.getMessage()));
        }
    }

    public Long getInstructorIdByEmail(String email) throws BusinessException {
        return instructorRepository.findIdByUsuarioEmail(email)
                .orElseThrow(() -> new BusinessException("No se encontró un instructor con ese email"));
    }

}