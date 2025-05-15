package com.unicolombo.bienestar.controllers;

import com.unicolombo.bienestar.dto.InstructorUpdateDto;
import com.unicolombo.bienestar.dto.RegistroHorasDto;
import com.unicolombo.bienestar.dto.RegistroInstructorDto;
import com.unicolombo.bienestar.models.Estudiante;
import com.unicolombo.bienestar.models.Instructor;
import com.unicolombo.bienestar.services.InscripcionService;
import com.unicolombo.bienestar.services.InstructorService;
import com.unicolombo.bienestar.services.RegistroHorasService;
import com.unicolombo.bienestar.exceptions.BusinessException;
import com.unicolombo.bienestar.utils.ResponseWrapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class InstructorController {

    private final InstructorService instructorService;
    private final RegistroHorasService registroHorasService;
    private InscripcionService inscripcionService;

    @Autowired
    public InstructorController(InstructorService instructorService, RegistroHorasService registroHorasService , InscripcionService inscripcionService ) {
        this.instructorService = instructorService;
        this.registroHorasService = registroHorasService;
        this.inscripcionService = inscripcionService;
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

    @PostMapping("/instructores/actividades/{actividadId}/registrar-horas")
    public ResponseEntity<?> registrarHoras(@PathVariable Long actividadId,
                                            @Valid @RequestBody RegistroHorasDto dto,
                                            BindingResult result) {
        if (result.hasErrors()) {
            String errorMessage = !result.getFieldErrors().isEmpty()
                    ? result.getFieldErrors().get(0).getDefaultMessage()
                    : "Error de validación";
            return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(errorMessage, result));
        }

        try {
            var registro = registroHorasService.registrarHoras(actividadId, dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ResponseWrapper.success(registro, "Horas registradas exitosamente"));
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

//    @GetMapping("/actividades/{actividadId}/estudiantes")
//    public ResponseEntity<?> obtenerEstudiantesInscritos(@PathVariable Long actividadId,
//                                                         @AuthenticationPrincipal UserDetails userDetails) {
//        Long instructorId = obtenerInstructorIdDesdeUsuario(userDetails.getUsername());
//
//        List<Estudiante> estudiantes = inscripcionService.obtenerEstudiantesInscritos(actividadId, instructorId);
//
//        if (estudiantes.isEmpty()) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(ResponseWrapper.error("No hay estudiantes inscritos o la actividad no te pertenece"));
//        }
//
//        return ResponseEntity.ok(ResponseWrapper.success(estudiantes, "Estudiantes inscritos encontrados"));
//    }

    // Puedes implementar este método como necesites si estás usando JWT o UserDetailsService
    private Long obtenerInstructorIdDesdeUsuario(String username) {
        // Ejemplo de obtención de instructor ID desde username
        return instructorService.obtenerIdPorCorreo(username);
    }
}
