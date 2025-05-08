package com.unicolombo.bienestar.controllers;

import com.unicolombo.bienestar.dto.LoginEstudianteRequest;
import com.unicolombo.bienestar.dto.LoginRequest;
import com.unicolombo.bienestar.dto.RefreshTokenRequest;
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
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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

            refreshTokenService.deleteByUsuario(usuario);

            String token = jwtService.generateToken(usuario);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(usuario);

            return ResponseEntity.ok(buildTokenResponse(token, refreshToken.getToken(), usuario));
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

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(usuario);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("refreshToken", refreshToken.getToken());
            response.put("usuario", Map.of(
                    "id", usuario.getId(),
                    "email", usuario.getEmail(),
                    "rol", usuario.getRol().name(),
                    "nombre", usuario.getNombre(),
                    "apellido", usuario.getApellido(),
                    "codigoEstudiantil", usuario.getEstudiante().getCodigoEstudiantil(),
                    "horasAcumuladas", usuario.getEstudiante().getHorasAcumuladas()
            ));

            return ResponseEntity.ok(response);
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


    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken())
                    .orElseThrow(() -> new RuntimeException("Refresh token inválido"));

            if (refreshTokenService.isTokenExpired(refreshToken)) {
                refreshTokenService.deleteByUsuario(refreshToken.getUsuario());
                throw new RuntimeException("Refresh token expirado");
            }

            RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken);
            String newJwt = jwtService.generateToken(refreshToken.getUsuario());

            return ResponseEntity.ok(buildTokenResponse(newJwt, newRefreshToken.getToken(), refreshToken.getUsuario()));
        } catch (RuntimeException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }


    private Map<String, Object> buildTokenResponse(String token, String refreshToken, Usuario usuario) {
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("refreshToken", refreshToken);
        response.put("usuario", buildUsuarioResponse(usuario));
        return response;
    }

    private Map<String, Object> buildUsuarioResponse(Usuario usuario) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", usuario.getId());
        userMap.put("email", usuario.getEmail());
        userMap.put("rol", usuario.getRol().name());
        userMap.put("nombre", usuario.getNombre());
        userMap.put("apellido", usuario.getApellido());

        if (usuario.getEstudiante() != null) {
            userMap.put("codigoEstudiantil", usuario.getEstudiante().getCodigoEstudiantil());
            userMap.put("horasAcumuladas", usuario.getEstudiante().getHorasAcumuladas());
        }

        return userMap;
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", message);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(status).body(response);
    }
}