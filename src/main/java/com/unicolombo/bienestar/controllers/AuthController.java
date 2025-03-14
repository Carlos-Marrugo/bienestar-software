package com.unicolombo.bienestar.controllers;

import com.unicolombo.bienestar.dto.LoginRequest;
import com.unicolombo.bienestar.controllers.*;
import com.unicolombo.bienestar.services.AdminService;
import com.unicolombo.bienestar.services.JwtService;
import com.unicolombo.bienestar.services.UsuarioServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioServices usuarioService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        boolean autenticado = false;
        String rol = "";
        String subject = "";

        // Autenticar como estudiante
        if (loginRequest.getEmail() != null && loginRequest.getCodigoEstudiantil() != null) {
            autenticado = usuarioService.authenticateEstudiante(loginRequest.getEmail(), loginRequest.getCodigoEstudiantil());
            rol = "ESTUDIANTE";
            subject = loginRequest.getEmail();
        }
        // Autenticar como admin
        else if (loginRequest.getUsername() != null && loginRequest.getPassword() != null) {
            autenticado = adminService.authenticateAdmin(loginRequest.getUsername(), loginRequest.getPassword());
            rol = "ADMIN";
            subject = loginRequest.getUsername();
        }

        if (autenticado) {
            String token = jwtService.generateToken(subject, rol);
            return ResponseEntity.ok(new LoginResponse(token));
        } else {
            return ResponseEntity.status(401).body(new LoginResponse("Credenciales inválidas"));
        }
    }
}