package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.dto.estudiante.*;
import com.unicolombo.bienestar.exceptions.BusinessException;
import com.unicolombo.bienestar.models.*;
import com.unicolombo.bienestar.repositories.*;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;

import static com.unicolombo.bienestar.models.EstadoEstudiante.INACTIVO;
import static com.unicolombo.bienestar.models.EstadoEstudiante.SUSPENDIDO;


@Service
@RequiredArgsConstructor
@Transactional
public class EstudianteService {

    private final UsuarioRepository usuarioRepo;
    private final EstudianteRepository estudianteRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SuspencionRepository suspensionRepo;
    private final AuditoriaService auditoriaService;


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

    public Page<EstudianteDto> listarPorEstado(EstadoEstudiante estado, Pageable pageable, String filtro) {
        if (filtro != null && !filtro.isEmpty()) {
            return estudianteRepo.findByEstadoAndFiltro(estado, filtro, pageable)
                    .map(this::convertToDto);
        }
        return estudianteRepo.findByEstado(estado, pageable)
                .map(this::convertToDto);
    }

    public Page<Estudiante> listarEstudiantes(Pageable pageable, String filtro) {
        if (filtro != null && !filtro.isEmpty()) {
            return estudianteRepo.findByFiltro(filtro, pageable);
        }
        return estudianteRepo.findAll(pageable);
    }

    @Transactional
    public void cambiarEstado(Long id, CambiarEstadoDto dto, Usuario administrador) {
        if (dto == null || dto.getEstado() == null || dto.getMotivo() == null) {
            throw new BusinessException("Datos de cambio de estado inválidos");
        }

        Estudiante estudiante = estudianteRepo.findById(id)
                .orElseThrow(() -> new BusinessException("Estudiante no encontrado"));

        if (estudiante.getEstado().equals(dto.getEstado())) {
            throw new BusinessException("El estudiante ya está en estado " + dto.getEstado());
        }

        if (dto.getEstado().equals(EstadoEstudiante.SUSPENDIDO)) {
            Suspension suspension = new Suspension();
            suspension.setEstudiante(estudiante);
            suspension.setAdministrador(administrador);
            suspension.setMotivo(dto.getMotivo());
            suspension.setFecha(LocalDateTime.now());

            if (dto.getFechaFin() != null) {
                suspension.setFechaFin(dto.getFechaFin());
            }

            suspensionRepo.save(suspension);
        }

        TipoAccion accion = TipoAccion.ACTUALIZACION;
        String detalles = String.format(
                "Estudiante ID: %d | Motivo: %s | Admin: %s",
                id,
                dto.getMotivo(),
                administrador.getEmail()
        );

        auditoriaService.registrarAccion(administrador, accion, detalles);

        estudiante.setEstado(dto.getEstado());
        estudianteRepo.save(estudiante);
    }


    private EstudiantePerfilDto mapToPerfilDto(Estudiante estudiante) {
        EstudiantePerfilDto dto = new EstudiantePerfilDto();
        Usuario usuario = estudiante.getUsuario();

        dto.setId(estudiante.getId());
        dto.setNombre(usuario.getNombre());
        dto.setApellido(usuario.getApellido());
        dto.setNombreCompleto(estudiante.getNombreCompleto());
        dto.setEmail(usuario.getEmail());

        dto.setCodigoEstudiantil(estudiante.getCodigoEstudiantil());
        dto.setProgramaAcademico(estudiante.getProgramaAcademico());
        dto.setSemestre(estudiante.getSemestre());
        dto.setHorasAcumuladas(estudiante.getHorasAcumuladas());
        dto.setEstado(estudiante.getEstado());

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

        Usuario usuario = estudiante.getUsuario();
        if(dto.getNombre() != null) {
            usuario.setNombre(dto.getNombre());
        }
        if(dto.getApellido() != null) {
            usuario.setApellido(dto.getApellido());
        }
        usuarioRepo.save(usuario);

        return estudianteRepo.save(estudiante);
    }


    //eliminar estudiante
    @Transactional
    public void eliminarEstudiante(Long id) {
        Estudiante estudiante = estudianteRepo.findById(id)
                .orElseThrow(() -> new BusinessException("Estudiante no encontrado"));

        estudiante.setEstado(INACTIVO);
        estudianteRepo.save(estudiante);
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

    public Page<EstudianteDto> listarEstudiantesPorInstructor(Long instructorId, Pageable pageable) {
        return estudianteRepo.findByInscripcionesActividadInstructorId(instructorId, pageable)
                .map(this::convertToDto);
    }
}