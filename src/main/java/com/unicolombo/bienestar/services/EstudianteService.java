package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.dto.RegistroEstudianteDto;
import com.unicolombo.bienestar.models.Estudiante;
import com.unicolombo.bienestar.models.Role;
import com.unicolombo.bienestar.models.Usuario;
import com.unicolombo.bienestar.repositories.EstudianteRepository;
import com.unicolombo.bienestar.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EstudianteService {

    private final UsuarioRepository usuarioRepo;
    private final EstudianteRepository estudianteRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Autowired
    public EstudianteService(UsuarioRepository usuarioRepo,
                             EstudianteRepository estudianteRepo,
                             PasswordEncoder passwordEncoder,
                             EmailService emailService) {
        this.usuarioRepo = usuarioRepo;
        this.estudianteRepo = estudianteRepo;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public Estudiante registrarEstudiante(RegistroEstudianteDto dto) {
        Usuario usuario = new Usuario();
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setRol(Role.ESTUDIANTE);
        usuario = usuarioRepo.save(usuario);

        Estudiante estudiante = new Estudiante();
        estudiante.setUsuario(usuario);
        estudiante.setCodigoEstudiantil(dto.getCodigoEstudiantil());
        estudiante.setHorasAcumuladas(0);
        estudiante = estudianteRepo.save(estudiante);

        // email de bienvenida
        emailService.sendWelcomeEmail(
                usuario.getEmail(),
                usuario.getNombre(),
                dto.getCodigoEstudiantil()
        );

        return estudiante;
    }
}