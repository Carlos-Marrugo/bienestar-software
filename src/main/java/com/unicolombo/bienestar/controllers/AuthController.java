package com.unicolombo.bienestar.controllers;


import com.unicolombo.bienestar.services.JwtService;
import com.unicolombo.bienestar.services.UsuarioDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioDetailsService usuarioDetailsService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        // Autentica al usuario
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        // Carga los detalles del usuario
        UserDetails userDetails = usuarioDetailsService.loadUserByUsername(loginRequest.getEmail());

        // Genera el token JWT
        String token = jwtService.generateToken(userDetails);

        // Devuelve el token en la respuesta
        return ResponseEntity.ok(new LoginResponse(token));
    }
}