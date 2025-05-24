package com.unicolombo.bienestar.controllers;

import com.unicolombo.bienestar.dto.InstructorUpdateDto;
import com.unicolombo.bienestar.dto.RegistroInstructorDto;
import com.unicolombo.bienestar.models.Instructor;
import com.unicolombo.bienestar.repositories.InstructorRepository;
import com.unicolombo.bienestar.services.InstructorService;
import com.unicolombo.bienestar.exceptions.BusinessException;
import com.unicolombo.bienestar.utils.ResponseWrapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    private final InstructorService instructorService;

    @Autowired
    private InstructorRepository instructorRepository;

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
    public ResponseEntity<?> listarInstructores(
            @RequestParam(defaultValue = "1") int currentPage,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Pageable pageable = PageRequest.of(currentPage - 1, size);
            Page<Instructor> instructores = instructorRepository.findAllActive(pageable);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", instructores.getContent(),
                    "pagination", Map.of(
                            "totalItems", instructores.getTotalElements(),
                            "totalPages", instructores.getTotalPages(),
                            "currentPage", currentPage
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "Error al listar instructores"
            ));
        }
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



}