package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.models.Usuario;
import com.unicolombo.bienestar.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public String authenticarUsuario(String correo, String password) {
        Usuario usuario = usuarioRepository.findByEmail(correo)
                .orElseThrow(()-> new RuntimeException("Usuario no encontrado!"));

        if (passwordEncoder.matches(password, usuario.getPassword())) {
            return "Token";
        } else {
            throw new RuntimeException("Credenciales invalidas!");
        }

    }

}
