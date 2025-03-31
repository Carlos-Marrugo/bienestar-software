package com.unicolombo.bienestar.controllers;

import com.unicolombo.bienestar.dto.LoginEstudianteRequest;
import com.unicolombo.bienestar.dto.LoginRequest;
import com.unicolombo.bienestar.models.Actividad;
import com.unicolombo.bienestar.models.Role;
import com.unicolombo.bienestar.models.Usuario;
import com.unicolombo.bienestar.services.AuthService;
import com.unicolombo.bienestar.services.JwtService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Usuario usuario = authService.authenticate(request.getEmail(), request.getPassword());

            if (usuario.getRol() == Role.ESTUDIANTE) {
                throw new RuntimeException("Los estudiantes deben usar el endpoint especial de login");
            }

            String token = jwtService.generateToken(usuario);

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "usuario", Map.of(
                            "id", usuario.getId(),
                            "email", usuario.getEmail(),
                            "rol", usuario.getRol().name(),
                            "nombre", usuario.getNombre(),
                            "apellido", usuario.getApellido()
                    )
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    //logeo como estudiante
    @PostMapping("/login-estudiante")
    public ResponseEntity<?> loginEstudiante(@Valid @RequestBody LoginEstudianteRequest request) {
        Usuario usuario = authService.authenticateEstudiante(
                request.getEmail(),
                request.getCodigoEstudiantil()
        );

        String token = jwtService.generateToken(usuario);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "usuario", Map.of(
                        "id", usuario.getId(),
                        "email", usuario.getEmail(),
                        "codigoEstudiantil", usuario.getEstudiante().getCodigoEstudiantil()
            )
        ));
    }
}