package com.unicolombo.bienestar.services;

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



    public Usuario authenticate(String email, String credencial) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        switch(usuario.getRol()) {
            case ESTUDIANTE:
                if (usuario.getEstudiante() == null ||
                        !usuario.getEstudiante().getCodigoEstudiantil().equals(credencial)) {
                    throw new RuntimeException("Código estudiantil incorrecto");
                }
                break;

            case ADMIN:
            case INSTRUCTOR:
                if (!passwordEncoder.matches(credencial, usuario.getPassword())) {
                    throw new RuntimeException("Contraseña incorrecta");
                }
                break;
        }

        return usuario;
    }
}