package com.unicolombo.bienestar.controllers;

import com.unicolombo.bienestar.services.AuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", auditoriaService.obtenerUltimas5Auditorias()
        ));
    }
}