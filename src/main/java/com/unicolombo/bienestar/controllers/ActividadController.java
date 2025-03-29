package com.unicolombo.bienestar.controllers;

import com.unicolombo.bienestar.dto.ActividadCreateDto;
import com.unicolombo.bienestar.models.Actividad;
import com.unicolombo.bienestar.services.ActividadService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/actividades")
@RequiredArgsConstructor
public class ActividadController {


    private final ActividadService actividadService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Actividad> crearActividad(@RequestBody ActividadCreateDto dto){
        Actividad actividad = actividadService.crearActividad(dto);
        return ResponseEntity.ok(actividad);

    }

    /*
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<List<Actividad>> listarActividades() {
        return ResponseEntity.ok(actividadService.listarTodas());
    }
    */


}
