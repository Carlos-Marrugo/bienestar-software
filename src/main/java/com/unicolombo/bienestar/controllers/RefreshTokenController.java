package com.unicolombo.bienestar.controllers;

import com.unicolombo.bienestar.dto.RefreshTokenRequest;
import com.unicolombo.bienestar.models.RefreshToken;
import com.unicolombo.bienestar.models.Usuario;
import com.unicolombo.bienestar.services.JwtService;
import com.unicolombo.bienestar.services.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class RefreshTokenController {

    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @Autowired
    public RefreshTokenController(RefreshTokenService refreshTokenService, JwtService jwtService) {
        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String requestToken = request.get("refreshToken");

        if (requestToken == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Refresh token es requerido"));
        }

        return refreshTokenService.findByToken(requestToken)
                .map(token -> {
                    Usuario usuario = token.getUsuario();

                    // Verificar si el token ha expirado
                    if (refreshTokenService.isTokenExpired(token)) {
                        refreshTokenService.deleteByUsuario(usuario);
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(Map.of("error", "Refresh token expirado"));
                    }

                    // Usar el usuario como UserDetails para generar un nuevo JWT
                    String newJwt = jwtService.generateToken(usuario);

                    // Crear un nuevo RefreshToken
                    RefreshToken newRefresh = refreshTokenService.createRefreshToken(usuario);

                    Map<String, String> response = new HashMap<>();
                    response.put("token", newJwt);
                    response.put("refreshToken", newRefresh.getToken());

                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Token inválido")));
    }
}