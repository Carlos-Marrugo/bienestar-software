package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.models.*;
import com.unicolombo.bienestar.repositories.InstructorRepository;
import com.unicolombo.bienestar.repositories.UsuarioRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class DataInitializer {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private InstructorRepository instructorRepository; // Asegúrate de tener este repositorio

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        // Crear ADMIN (opcional)
    /*
        Usuario admin = new Usuario();
        admin.setEmail("nicolle.lopezrivera@unicolombo.edu.co");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRol(Role.ADMIN);
        admin.setNombre("Nicolle Andrea");
        admin.setApellido("Lopez Alcazar");
        usuarioRepository.save(admin);*/



        /*
        // Crear INSTRUCTOR - Daniel Castilla
        Usuario instructorUser = new Usuario();
        instructorUser.setEmail("daniel@unicolombo.edu.co");
        instructorUser.setPassword(passwordEncoder.encode("daniel123"));
        instructorUser.setRol(Role.INSTRUCTOR);
        instructorUser.setNombre("Daniel");
        instructorUser.setApellido("Castilla");
        usuarioRepository.save(instructorUser); // Primero guardamos el Usuario

        Instructor instructor = new Instructor();
        instructor.setUsuario(instructorUser); // Asociamos el Usuario
        instructor.setEspecialidad("Entrenamiento Funcional");
        instructor.setFechaContratacion(LocalDate.now());
        instructorRepository.save(instructor); // Luego guardamos el Instructor

        // Crear INSTRUCTOR - Ashley Riff
        Usuario instructorUser1 = new Usuario();
        instructorUser1.setEmail("ashley@unicolombo.edu.co"); // Corrección: minúscula en email
        instructorUser1.setPassword(passwordEncoder.encode("ashley123"));
        instructorUser1.setRol(Role.INSTRUCTOR);
        instructorUser1.setNombre("Ashley");
        instructorUser1.setApellido("Riff");
        usuarioRepository.save(instructorUser1);

        Instructor instructor1 = new Instructor();
        instructor1.setUsuario(instructorUser1);
        instructor1.setEspecialidad("Yoga y Pilates");
        instructor1.setFechaContratacion(LocalDate.now());
        instructorRepository.save(instructor1);
        */

    }
}