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
public class EstudianteService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void registrarEstudiante(RegistroEstudianteDto dto) {
        Usuario usuario = new Usuario();
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setRol(Role.ESTUDIANTE);

        usuarioRepository.save(usuario);

        Estudiante estudiante = new Estudiante();
        estudiante.setUsuario(usuario);
        estudiante.setCodigoEstudiantil(dto.getCodigoEstudiantil());
        estudianteRepository.save(estudiante);

    }

}
