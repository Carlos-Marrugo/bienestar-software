package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.models.Role;
import com.unicolombo.bienestar.models.Usuario;
import com.unicolombo.bienestar.repositories.UsuarioRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class DataInitializer {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        // Crear ADMIN
        Usuario admin = new Usuario();
        admin.setEmail("admin@unicolombo.edu.co");
        admin.setPassword(passwordEncoder.encode("admin123")); // ¡Asegúrate de codificar!
        admin.setRol(Role.ADMIN);
        admin.setNombre("Admin");
        admin.setApellido("Principal");
        usuarioRepository.save(admin);

        // Crear INSTRUCTOR
        Usuario instructor = new Usuario();
        instructor.setEmail("instructor@unicolombo.edu.co");
        instructor.setPassword(passwordEncoder.encode("instructor123"));
        instructor.setRol(Role.INSTRUCTOR);
        instructor.setNombre("Carlos");
        instructor.setApellido("Martinez");
        usuarioRepository.save(instructor);
    }
}
