package com.unicolombo.bienestar.controllers;

import com.unicolombo.bienestar.dto.*;
import com.unicolombo.bienestar.dto.estudiante.*;
import com.unicolombo.bienestar.exceptions.BusinessException;
import com.unicolombo.bienestar.models.EstadoEstudiante;
import com.unicolombo.bienestar.models.Estudiante;
import com.unicolombo.bienestar.models.Usuario;
import com.unicolombo.bienestar.repositories.UsuarioRepository;
import com.unicolombo.bienestar.services.EstudianteService;
import com.unicolombo.bienestar.services.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/estudiantes")
@RequiredArgsConstructor
@Tag(name = "Estudiantes", description = "Gestión de estudiantes")
@SecurityRequirement(name = "bearerAuth")
public class EstudianteController {

    @Autowired
    private EstudianteService estudianteService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/registro")
    @Operation(summary = "Registrar nuevo estudiante")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registrarEstudiante(
            @Valid @RequestBody RegistroEstudianteDto dto,
            @RequestHeader("Authorization") String authHeader,
            @AuthenticationPrincipal UserDetails userDetails) {

        String token = authHeader.substring(7);
        if (!jwtService.isTokenValid(token, userDetails)) {
            throw new BusinessException("Token inválido o expirado");
        }

        Estudiante estudiante = estudianteService.registrarEstudiante(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "status", "success",
                "data", estudiante,
                "message", "Estudiante registrado exitosamente"
        ));
    }


    @GetMapping("/{id}/perfil")
    @Operation(summary = "Obtener perfil completo")
    @PreAuthorize("hasAnyRole('ESTUDIANTE', 'ADMIN')")
    public ResponseEntity<?> obtenerPerfil(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ESTUDIANTE"))) {

            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

            if (!usuario.getEstudiante().getId().equals(id)) {
                throw new BusinessException("No autorizado para ver este perfil");
            }
        }

        EstudiantePerfilDto perfil = estudianteService.obtenerPerfilCompleto(id);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", perfil
        ));
    }

    /*@GetMapping("/{id}/horas-actividades")
    @Operation(summary = "Obtener horas por actividad")
    @PreAuthorize("hasAnyRole('ESTUDIANTE', 'ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<?> obtenerHorasActividades(@PathVariable Long id) {
        List<HorasActividadDto> horas = estudianteService.obtenerHorasPorActividad(id);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", horas,
                "meta", Map.of("total", horas.size())
        ));
    }*/

    @GetMapping
    @Operation(summary = "Listar todos los estudiantes (Admin)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listarEstudiantes(
            Pageable pageable,
            @RequestParam(required = false) String filtro) {

        Page<Estudiante> estudiantes = estudianteService.listarEstudiantes(pageable, filtro);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", estudiantes.getContent(),
                "meta", Map.of(
                        "total", estudiantes.getTotalElements(),
                        "page", estudiantes.getNumber(),
                        "size", estudiantes.getSize()
                )
        ));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado de estudiante")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cambiarEstadoEstudiante(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        Usuario administrador = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("Usuario administrador no encontrado"));

        estudianteService.cambiarEstado(id, dto, administrador);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Estado del estudiante actualizado a " + dto.getEstado()
        ));
    }

    //listado de estudiantes por estado
    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listarPorEstado(
            @PathVariable EstadoEstudiante estado,
            Pageable pageable,
            @RequestParam(required = false) String filtro) {

        Page<EstudianteDto> estudiantes = estudianteService.listarPorEstado(estado, pageable, filtro);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", estudiantes.getContent(),
                "meta", Map.of(
                        "total", estudiantes.getTotalElements(),
                        "page", estudiantes.getNumber(),
                        "size", estudiantes.getSize()
                )
        ));
    }


    @PutMapping("/{id}")
    @Operation(summary = "Actualizar estudiante (Admin)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizarEstudiante(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEstudianteDto dto) {

        Estudiante estudiante = estudianteService.actualizarEstudiante(id, dto);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", estudiante,
                "message", "Estudiante actualizado"
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> eliminarEstudiante(@PathVariable Long id) {
        estudianteService.eliminarEstudiante(id);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Se ha cambiado este estudiante a INACTIVO"
        ));
    }

    @GetMapping("/instructor/{instructorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<?> listarEstudiantesPorInstructor(
            @PathVariable Long instructorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<EstudianteDto> estudiantes = estudianteService.listarEstudiantesPorInstructor(instructorId, pageable);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", estudiantes.getContent(),
                "meta", Map.of(
                        "total", estudiantes.getTotalElements(),
                        "page", estudiantes.getNumber(),
                        "size", estudiantes.getSize()
                )
        ));
    }
}