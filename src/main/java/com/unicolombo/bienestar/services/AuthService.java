package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.models.Usuario;
import com.unicolombo.bienestar.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public Usuario authenticateUser(String email, String codigoEstudiantil) {

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));


        if (!usuario.getCodigoEstudiantil().equals(codigoEstudiantil)) {
            throw new RuntimeException("Código estudiantil incorrecto");
        }


        if (!usuario.getRol().equals("ESTUDIANTE")) {
            throw new RuntimeException("Solo los estudiantes pueden iniciar sesión con código estudiantil");
        }

        return usuario;
    }
}