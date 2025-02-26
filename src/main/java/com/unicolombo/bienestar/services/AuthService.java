package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.models.Usuario;
import com.unicolombo.bienestar.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements UserDetailsService {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return User.withUsername(usuario.getEmail())
                .password(usuario.getPassword())
                .roles(usuario.getRol())
                .build();
    }

    public String autenticarUsuario(String correo, String password) {
        UserDetails userDetails = loadUserByUsername(correo);

        if (passwordEncoder.matches(password, userDetails.getPassword())) {
            // Aquí se generará el token JWT (lo implementaremos después)
            return "TokenGenerado";
        } else {
            throw new RuntimeException("Credenciales inválidas");
        }
    }
}