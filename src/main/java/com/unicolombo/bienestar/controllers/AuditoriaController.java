package com.unicolombo.bienestar.controllers;

import com.unicolombo.bienestar.dto.AuditoriaDto;
import com.unicolombo.bienestar.services.AuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auditoria")
@Tag(name = "Auditoría", description = "Registro de acciones del sistema")
@RequiredArgsConstructor
public class AuditoriaController {
    private final AuditoriaService auditoriaService;

    @GetMapping("/ultimas")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener las últimas 5 acciones registradas")
    public ResponseEntity<?> obtenerUltimasAcciones() {
        List<AuditoriaDto> auditorias = auditoriaService.obtenerUltimas5Auditorias().stream()
                .map(AuditoriaDto::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", auditorias
        ));
    }
}