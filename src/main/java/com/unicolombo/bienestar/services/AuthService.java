package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.dto.LoginRequest;
import com.unicolombo.bienestar.models.Usuario;
import com.unicolombo.bienestar.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Usuario authenticate(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // auth para estudiantes
        if (usuario.getRol().equals("ESTUDIANTE")) {
            if (!usuario.getCodigoEstudiantil().equals(request.getCodigoEstudiantil())) {
                throw new RuntimeException("Codigo estudiantil incorrecto");
            }
            return usuario;
        }
        // auth para admin/instructores
        else if (usuario.getRol().equals("ADMIN") || usuario.getRol().equals("INSTRUCTOR")) {
            if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
                throw new RuntimeException("Contraseña incorrecta");
            }
            return usuario;
        }

        throw new RuntimeException("Tipo de usuario no válido");
    }
}