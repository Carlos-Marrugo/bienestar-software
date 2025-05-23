package com.unicolombo.bienestar.controllers;

import com.unicolombo.bienestar.dto.InscripcionCreateDto;
import com.unicolombo.bienestar.dto.ResponseDto;
import com.unicolombo.bienestar.dto.estudiante.EstudianteInscritoDto;
import com.unicolombo.bienestar.exceptions.BusinessException;
import com.unicolombo.bienestar.models.*;
import com.unicolombo.bienestar.repositories.UsuarioRepository;
import com.unicolombo.bienestar.services.InscripcionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Slf4j
@Tag(name = "Inscripciones", description = "Gestión de inscripciones a actividades")
public class InscripcionController {

    @Autowired
    private InscripcionService inscripcionService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Operation(summary = "Inscribir estudiante a una actividad",
            description = "Permite a un estudiante inscribirse en una actividad con todos sus horarios definidos")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Inscripción creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Estudiante o actividad no encontrados")
    })
    @PostMapping("/estudiantes/{estudianteId}/inscribirse/{actividadId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTUDIANTE')")
    public ResponseEntity<?> inscribirEstudiante(
            @PathVariable Long estudianteId,
            @PathVariable Long actividadId,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            log.info("Procesando solicitud de inscripción para estudiante {} en actividad {}", estudianteId, actividadId);

            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

            if (usuario.getRol() == Role.ESTUDIANTE && !usuario.getEstudiante().getId().equals(estudianteId)) {
                throw new AccessDeniedException("Solo puedes inscribirte a ti mismo");
            }

            InscripcionCreateDto dto = new InscripcionCreateDto();
            dto.setEstudianteId(estudianteId);
            dto.setActividadId(actividadId);

            Inscripcion inscripcion = inscripcionService.crearInscripcion(dto, userDetails.getUsername());

            // Crear una respuesta más detallada que incluya la información de horarios
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("status", "success");
            respuesta.put("message", "Inscripción creada exitosamente");

            Map<String, Object> data = new HashMap<>();
            data.put("inscripcionId", inscripcion.getId());
            data.put("estudianteId", inscripcion.getEstudiante().getId());
            data.put("actividadId", inscripcion.getActividad().getId());
            data.put("fechaInscripcion", inscripcion.getFechaInscripcion());

            // Añadir información de horarios si existe
            if (inscripcion.getActividad().getUbicacion() != null &&
                    inscripcion.getActividad().getUbicacion().getHorarios() != null) {

                data.put("horarios", inscripcion.getActividad().getUbicacion().getHorarios().stream()
                        .map(horario -> Map.of(
                                "id", horario.getId(),
                                "dia", horario.getDia(),
                                "horaInicio", horario.getHoraInicio(),
                                "horaFin", horario.getHoraFin()
                        ))
                        .collect(Collectors.toList()));

                data.put("totalHorarios", inscripcion.getActividad().getUbicacion().getHorarios().size());
            }

            respuesta.put("data", data);

            log.info("Inscripción procesada exitosamente con ID: {}", inscripcion.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
        } catch (BusinessException e) {
            log.error("Error de negocio al inscribir estudiante: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        } catch (AccessDeniedException e) {
            log.error("Error de acceso al inscribir estudiante: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error inesperado al inscribir estudiante", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "Error al procesar la inscripción: " + e.getMessage()
            ));
        }
    }

    @Operation(summary = "Listar estudiantes inscritos en una actividad",
            description = "Permite a un instructor o admin ver los estudiantes inscritos en una actividad")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado exitoso"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Actividad no encontrada")
    })

    @GetMapping("/instructores/actividades/{actividadId}/estudiantes")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<?> listarEstudiantesInscritos(
            @PathVariable Long actividadId,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            log.info("Solicitando lista de estudiantes inscritos para actividad ID: {}", actividadId);
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

            if (usuario.getRol() == Role.INSTRUCTOR) {
                boolean esInstructorDeActividad = inscripcionService.verificarInstructorDeActividad(
                        usuario.getId(), actividadId);

                if (!esInstructorDeActividad) {
                    log.warn("Usuario {} intentó acceder a información de actividad {} sin ser el instructor",
                            usuario.getEmail(), actividadId);
                    throw new AccessDeniedException("No eres instructor de esta actividad");
                }
            }

            List<Inscripcion> inscripciones = inscripcionService.obtenerInscripcionesPorActividad(actividadId);
            log.info("Encontradas {} inscripciones para la actividad {}", inscripciones.size(), actividadId);

            List<Map<String, Object>> estudiantes = inscripciones.stream()
                    .map(inscripcion -> {
                        Estudiante estudiante = inscripcion.getEstudiante();
                        Map<String, Object> estudianteData = new HashMap<>();
                        estudianteData.put("inscripcionId", inscripcion.getId());
                        estudianteData.put("estudiante", Map.of(
                                "id", estudiante.getId(),
                                "nombre", estudiante.getNombreCompleto(),
                                "codigo", estudiante.getCodigoEstudiantil(),
                                "programa", estudiante.getProgramaAcademico(),
                                "fechaInscripcion", inscripcion.getFechaInscripcion(),
                                "horasRegistradas", inscripcion.getHorasRegistradas()
                        ));

                        // Añadir información de horarios si existe
                        if (inscripcion.getActividad().getUbicacion() != null &&
                                inscripcion.getActividad().getUbicacion().getHorarios() != null) {

                            estudianteData.put("horarios", inscripcion.getActividad().getUbicacion().getHorarios().stream()
                                    .map(horario -> Map.of(
                                            "dia", horario.getDia(),
                                            "horaInicio", horario.getHoraInicio(),
                                            "horaFin", horario.getHoraFin()
                                    ))
                                    .collect(Collectors.toList()));
                        }

                        return estudianteData;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", estudiantes,
                    "meta", Map.of(
                            "actividadId", actividadId,
                            "total", estudiantes.size()
                    )
            ));
        } catch (BusinessException e) {
            log.error("Error de negocio al listar estudiantes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        } catch (AccessDeniedException e) {
            log.error("Error de acceso al listar estudiantes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error inesperado al listar estudiantes inscritos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "Error al procesar la solicitud: " + e.getMessage()
            ));
        }
    }

    @Operation(summary = "Cancelar inscripción", description = "Permite cancelar una inscripción")
    @DeleteMapping("/inscripciones/{inscripcionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTUDIANTE')")
    public ResponseEntity<?> cancelarInscripcion(
            @PathVariable Long inscripcionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

            if (usuario.getRol() == Role.ESTUDIANTE) {
                Inscripcion inscripcion = inscripcionService.obtenerInscripcion(inscripcionId);
                if (!inscripcion.getEstudiante().getUsuario().getId().equals(usuario.getId())) {
                    throw new AccessDeniedException("No puedes cancelar inscripciones de otros estudiantes");
                }
            }

            inscripcionService.cancelarInscripcion(inscripcionId, userDetails.getUsername());

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Inscripción cancelada exitosamente"
            ));
        } catch (BusinessException e) {
            log.error("Error de negocio al cancelar inscripción: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        } catch (AccessDeniedException e) {
            log.error("Error de acceso al cancelar inscripción: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error inesperado al cancelar inscripción", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "Error al cancelar la inscripción: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/instructores/{id}/estudiantes")
    public ResponseEntity<ResponseDto> getEstudiantesInscritos(
            @PathVariable("id") Long instructorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<EstudianteInscritoDto> estudiantes = inscripcionService.getEstudiantesInscritosByInstructor(instructorId, pageable);
        return ResponseEntity.ok(ResponseDto.success("Estudiantes recuperados exitosamente", estudiantes));
    }
}