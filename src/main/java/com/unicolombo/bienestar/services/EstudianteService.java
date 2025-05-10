package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.dto.estudiante.ActualizarEstudianteDto;
import com.unicolombo.bienestar.dto.estudiante.EstudianteDto;
import com.unicolombo.bienestar.dto.estudiante.EstudiantePerfilDto;
import com.unicolombo.bienestar.dto.estudiante.RegistroEstudianteDto;
import com.unicolombo.bienestar.exceptions.BusinessException;
import com.unicolombo.bienestar.models.*;
import com.unicolombo.bienestar.repositories.*;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import org.springframework.data.domain.Pageable;



@Service
@RequiredArgsConstructor
@Transactional
public class EstudianteService {

    private final UsuarioRepository usuarioRepo;
    private final EstudianteRepository estudianteRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public Estudiante registrarEstudiante(RegistroEstudianteDto dto) {
        validarCorreoInstitucional(dto.getEmail());

        if (usuarioRepo.existsByEmail(dto.getEmail())) {
            throw new BusinessException("El email ya está registrado");
        }

        if (estudianteRepo.existsByCodigoEstudiantil(dto.getCodigoEstudiantil())) {
            throw new BusinessException("El código estudiantil ya existe");
        }

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
        estudiante.setProgramaAcademico(dto.getProgramaAcademico());
        estudiante.setSemestre(dto.getSemestre());
        estudiante.setEstado(EstadoEstudiante.ACTIVO);
        estudiante.setHorasAcumuladas(0);

        return estudianteRepo.save(estudiante);
    }

    public EstudiantePerfilDto obtenerPerfilCompleto(Long estudianteId) {
        Estudiante estudiante = estudianteRepo.findById(estudianteId)
                .orElseThrow(() -> new BusinessException("Estudiante no encontrado"));

        return mapToPerfilDto(estudiante);
    }

    //lista por estado activo o inactivo
    public Page<EstudianteDto> listarPorEstado(EstadoEstudiante estado, Pageable pageable, String filtro) {
        if (filtro != null && !filtro.isEmpty()) {
            return estudianteRepo.findByEstadoAndFiltro(estado, filtro, pageable)
                    .map(this::convertToDto);
        }
        return estudianteRepo.findByEstado(estado, pageable)
                .map(this::convertToDto);
    }

    public Estudiante cambiarEstado(Long id, EstadoEstudiante nuevoEstado) {
        Estudiante estudiante = estudianteRepo.findById(id)
                .orElseThrow(() -> new BusinessException("Estudiante no encontrado"));

        estudiante.setEstado(nuevoEstado);
        return estudianteRepo.save(estudiante);
    }


    private EstudiantePerfilDto mapToPerfilDto(Estudiante estudiante) {
        EstudiantePerfilDto dto = new EstudiantePerfilDto();
        dto.setId(estudiante.getId());
        dto.setNombreCompleto(estudiante.getNombreCompleto());
        dto.setEmail(estudiante.getUsuario().getEmail());
        dto.setCodigoEstudiantil(estudiante.getCodigoEstudiantil());
        dto.setProgramaAcademico(estudiante.getProgramaAcademico());
        dto.setSemestre(estudiante.getSemestre());
        dto.setEstado(estudiante.getEstado());

        // Comentado para implementación futura
        // dto.setActividades(...);

        return dto;
    }

    //public List<HorasActividadDto> obtenerHorasPorActividad(Long estudianteId) {
    //    return estudianteRepo.findHorasPorActividad(estudianteId);
    //}

    @Transactional
    public Estudiante actualizarEstudiante(Long id, ActualizarEstudianteDto dto) {
        Estudiante estudiante = estudianteRepo.findById(id)
                .orElseThrow(() -> new BusinessException("Estudiante no encontrado"));

        estudiante.setProgramaAcademico(dto.getProgramaAcademico());
        estudiante.setSemestre(dto.getSemestre());
        estudiante.setEstado(dto.getEstado());

        return estudianteRepo.save(estudiante);
    }

    public Page<Estudiante> listarEstudiantes(Pageable pageable, String filtro) {
        if (filtro != null && !filtro.isEmpty()) {
            return estudianteRepo.findByFiltro(filtro, pageable);
        }
        return estudianteRepo.findAll(pageable);
    }

    private EstudiantePerfilDto mapToPerfilDto(Estudiante estudiante, List<Inscripcion> inscripciones) {
        EstudiantePerfilDto dto = new EstudiantePerfilDto();
        dto.setId(estudiante.getId());
        dto.setNombreCompleto(estudiante.getNombreCompleto());
        dto.setEmail(estudiante.getUsuario().getEmail());
        dto.setCodigoEstudiantil(estudiante.getCodigoEstudiantil());
        dto.setProgramaAcademico(estudiante.getProgramaAcademico());
        dto.setSemestre(estudiante.getSemestre());
        dto.setHorasAcumuladas(estudiante.getHorasAcumuladas());
        dto.setEstado(estudiante.getEstado());
        return dto;
    }


    private void validarCorreoInstitucional(String email) {
        if (!email.endsWith("@unicolombo.edu.co")) {
            throw new BusinessException("Solo se permiten correos institucionales (@unicolombo.edu.co)");
        }
    }

    private EstudianteDto convertToDto(Estudiante estudiante) {
        EstudianteDto dto = new EstudianteDto();
        dto.setId(estudiante.getId());
        dto.setNombreCompleto(estudiante.getNombreCompleto());
        dto.setCodigoEstudiantil(estudiante.getCodigoEstudiantil());
        dto.setProgramaAcademico(estudiante.getProgramaAcademico());
        dto.setSemestre(estudiante.getSemestre());
        dto.setEstado(estudiante.getEstado());
        return dto;
    }
}