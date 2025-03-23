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
public class AuthService implements UserDetailsService {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;



    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {

        //Validar correo
        if (!correo.endsWith("@unicolombo.edu.co")) {
            throw new UsernameNotFoundException("Este usuario no hace parte de Unicolombo");
        }

        Usuario usuario = usuarioRepository.findByEmail(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return User.withUsername(usuario.getEmail())
                .password(usuario.getPassword())
                .roles(usuario.getRol())
                .build();
    }

        public String authenticarUsuario(String correo, String password) {


        if (!correo.endsWith("@unicolombo.edu.co")) {
            throw new RuntimeException("El correo no pertenece al dominio @unicolombo.edu.co");
        }

        UserDetails userDetails = loadUserByUsername(correo);

        if (passwordEncoder.matches(password, userDetails.getPassword())) {
            return "TokenGenerado";
        } else {
            throw new RuntimeException("Credenciales inválidas");
        }
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

}