package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.dto.RegistroEstudianteDto;
import com.unicolombo.bienestar.models.Estudiante;
import com.unicolombo.bienestar.models.Role;
import com.unicolombo.bienestar.models.Usuario;
import com.unicolombo.bienestar.repositories.EstudianteRepository;
import com.unicolombo.bienestar.repositories.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class EstudianteService {

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private EstudianteRepository estudianteRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

        return estudianteRepo.save(estudiante);
    }
}
