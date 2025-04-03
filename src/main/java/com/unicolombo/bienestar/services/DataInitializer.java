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
    /*
        Usuario admin = new Usuario();
        admin.setEmail("marrugo@unicolombo.edu.co");
        admin.setPassword(passwordEncoder.encode("marrugo123"));
        admin.setRol(Role.ADMIN);
        admin.setNombre("Admin");
        admin.setApellido("Vargas");
        usuarioRepository.save(admin);


*/
        // Crear INSTRUCTOR

        Usuario instructor = new Usuario();
        instructor.setEmail("annabelle@unicolombo.edu.co");
        instructor.setPassword(passwordEncoder.encode("annabelle123"));
        instructor.setRol(Role.INSTRUCTOR);
        instructor.setNombre("Annabelle");
        instructor.setApellido("Riffle");
        usuarioRepository.save(instructor);

        Usuario instructor1 = new Usuario();
        instructor1.setEmail("Ashley@unicolombo.edu.co");
        instructor1.setPassword(passwordEncoder.encode("ashley123"));
        instructor1.setRol(Role.INSTRUCTOR);
        instructor1.setNombre("Ashley");
        instructor1.setApellido("Riff");
        usuarioRepository.save(instructor);


    }
}
