package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.dto.LoginRequest;
import com.unicolombo.bienestar.models.Role;
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

    public Usuario authenticate(String email, String password) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        return usuario;
    }

    public Usuario authenticateEstudiante(String email, String codigo) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        if (usuario.getRol() != Role.ESTUDIANTE ||
                usuario.getEstudiante() == null ||
                !usuario.getEstudiante().getCodigoEstudiantil().equalsIgnoreCase(codigo)) {
            throw new RuntimeException("Credenciales inválidas");
        }

        return usuario;
    }
}