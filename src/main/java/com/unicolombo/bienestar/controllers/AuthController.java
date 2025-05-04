package com.unicolombo.bienestar.controllers;

import com.unicolombo.bienestar.dto.LoginEstudianteRequest;
import com.unicolombo.bienestar.dto.LoginRequest;
import com.unicolombo.bienestar.dto.ResetPasswordRequest;
import com.unicolombo.bienestar.models.RefreshToken;
import com.unicolombo.bienestar.models.Role;
import com.unicolombo.bienestar.models.Usuario;
import com.unicolombo.bienestar.repositories.UsuarioRepository;
import com.unicolombo.bienestar.services.AuthService;
import com.unicolombo.bienestar.services.JwtService;
import com.unicolombo.bienestar.services.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "1. Autenticación", description = "Endpoints para login de usuarios y estudiantes")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Operation(
            summary = "Login para administradores e instructores",
            description = "Autentica usuarios con rol ADMIN o INSTRUCTOR",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login exitoso",
                            content = @Content(schema = @Schema(implementation = Map.class))),
                    @ApiResponse(responseCode = "400", description = "Credenciales inválidas")
            }
    )
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Usuario usuario = authService.authenticate(request.getEmail(), request.getPassword());

            if (usuario.getRol() == Role.ESTUDIANTE) {
                throw new RuntimeException("Los estudiantes deben usar /login-estudiante");
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
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }

    @Operation(
            summary = "Login para estudiantes",
            description = "Autentica estudiantes usando su código estudiantil",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login exitoso",
                            content = @Content(schema = @Schema(implementation = Map.class))),
                    @ApiResponse(responseCode = "400", description = "Credenciales inválidas")
            }
    )
    @PostMapping("/login-estudiante")
    public ResponseEntity<?> loginEstudiante(@Valid @RequestBody LoginEstudianteRequest request) {
        try {
            Usuario usuario = authService.authenticate(
                    request.getEmail(),
                    request.getCodigoEstudiantil()
            );

            String token = jwtService.generateToken(usuario);

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "usuario", Map.of(
                            "id", usuario.getId(),
                            "email", usuario.getEmail(),
                            "rol", usuario.getRol().name(),
                            "nombre", usuario.getNombre(),
                            "apellido", usuario.getApellido(),
                            "codigoEstudiantil", usuario.getEstudiante().getCodigoEstudiantil(),
                            "horasAcumuladas", usuario.getEstudiante().getHorasAcumuladas()
                    )
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody LoginRequest request) {
        try {
            authService.sendResetPasswordEmail(request.getEmail());
            return ResponseEntity.ok(Map.of(
                    "message", "Correo enviado con instrucciones para restablecer la contraseña"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            String email = jwtService.extractUsername(request.getToken());
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("No existe un usuario con ese correo"));

            usuario.setPassword(passwordEncoder.encode(request.getNewPassword()));
            usuarioRepository.save(usuario);

            return ResponseEntity.ok(Map.of("message", "Contraseña restablecida exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Token inválido o expirado",
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }

}