package com.unicolombo.bienestar.controllers;

import com.unicolombo.bienestar.dto.LoginRequest;
import com.unicolombo.bienestar.services.UsuarioServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioServices usuarioService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        boolean autenticado = usuarioService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());
        if (autenticado) {
            return ResponseEntity.ok("Autenticación exitosa");
        } else {
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }
    }
}