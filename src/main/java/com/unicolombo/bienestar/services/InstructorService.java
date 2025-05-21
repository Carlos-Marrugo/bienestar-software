package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.dto.*;
import com.unicolombo.bienestar.dto.Actividad.ActividadInstructorDto;
import com.unicolombo.bienestar.dto.estudiante.EstudianteDto;
import com.unicolombo.bienestar.models.*;
import com.unicolombo.bienestar.repositories.*;
import com.unicolombo.bienestar.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InstructorService {

    private final UsuarioRepository usuarioRepository;
    private final InstructorRepository instructorRepository;
    private final ActividadRepository actividadRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    public Instructor registrarInstructor(RegistroInstructorDto dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("El email ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setRol(Role.INSTRUCTOR);
        usuario = usuarioRepository.save(usuario);

        Instructor instructor = new Instructor();
        instructor.setUsuario(usuario);
        instructor.setEspecialidad(dto.getEspecialidad());
        ZoneId zonaColombia = ZoneId.of("America/Bogota");
        instructor.setFechaContratacion(LocalDate.now(zonaColombia));

        return instructorRepository.save(instructor);
    }

    public Instructor actualizarInstructor(Long id, InstructorUpdateDto dto) {
        Instructor instructor = instructorRepository.findActiveById(id)
                .orElseThrow(() -> new BusinessException("Instructor no encontrado o inactivo"));

        instructor.setEspecialidad(dto.getEspecialidad());

        Usuario usuario = instructor.getUsuario();
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        usuarioRepository.save(usuario);
        return instructorRepository.save(instructor);
    }

    public void desactivarInstructor(Long id) {
        Instructor instructor = instructorRepository.findActiveById(id)
                .orElseThrow(() -> new BusinessException("Instructor no encontrado o ya inactivo"));

        if (!instructor.getActividades().isEmpty()) {
            throw new BusinessException("No se puede desactivar un instructor con actividades asignadas");
        }

        instructor.getUsuario().setActivo(false);
        usuarioRepository.save(instructor.getUsuario());
    }

    public List<InstructorListDto> listarInstructoresActivos() {
        return instructorRepository.findAllActive().stream()
                .map(InstructorListDto::new)
                .collect(Collectors.toList());
    }

    public Optional<InstructorDetailDto> obtenerInstructorDetalle(Long id) {
        return instructorRepository.findActiveById(id)
                .map(InstructorDetailDto::new);
    }


    public List<Actividad> getActividadesAsignadasRaw(Long instructorId) {
        Instructor instructor = instructorRepository.findByIdWithActividades(instructorId)
                .orElseThrow(() -> new BusinessException("Instructor no encontrado"));
        return instructor.getActividades();
    }

    public List<ActividadInstructorDto> getActividadesAsignadasFormateadas(Long instructorId) {
        List<Actividad> actividades = getActividadesAsignadasRaw(instructorId);
        return actividades.stream()
                .map(ActividadInstructorDto::new)
                .collect(Collectors.toList());
    }

    public List<EstudianteDto> getEstudiantesInscritosEnActividad(Long instructorId, Long actividadId) {
        Actividad actividad = actividadRepository.findById(actividadId)
                .orElseThrow(() -> new BusinessException("Actividad no encontrada"));

        if (!actividad.getInstructor().getId().equals(instructorId)) {
            throw new BusinessException("No estás asignado a esta actividad");
        }

        return inscripcionRepository.findByActividadId(actividadId, Pageable.unpaged()).stream()
                .map(inscripcion -> {
                    EstudianteDto dto = new EstudianteDto();
                    dto.setId(inscripcion.getEstudiante().getId());
                    dto.setNombreCompleto(inscripcion.getEstudiante().getNombreCompleto());
                    dto.setCodigoEstudiantil(inscripcion.getEstudiante().getCodigoEstudiantil());
                    dto.setProgramaAcademico(inscripcion.getEstudiante().getProgramaAcademico());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public Long getInstructorIdByEmail(String email) throws BusinessException {
        return instructorRepository.findIdByUsuarioEmail(email)
                .orElseThrow(() -> new BusinessException("No se encontró un instructor con ese email"));
    }

    public InstructorPerfilDto obtenerPerfilInstructor(Long id) {
        Instructor instructor = instructorRepository.findActiveById(id)
                .orElseThrow(() -> new BusinessException("Instructor no encontrado o inactivo"));

        return mapToPerfilDto(instructor);
    }

    private InstructorPerfilDto mapToPerfilDto(Instructor instructor) {
        InstructorPerfilDto dto = new InstructorPerfilDto();
        dto.setId(instructor.getId());
        dto.setNombre(instructor.getUsuario().getNombre());
        dto.setApellido(instructor.getUsuario().getApellido());
        dto.setEmail(instructor.getUsuario().getEmail());
        dto.setEspecialidad(instructor.getEspecialidad());
        dto.setFechaContratacion(instructor.getFechaContratacion().toString());

        return dto;
    }

    public InstructorPerfilDto obtenerPerfilPorEmail(String email) {
        Instructor instructor = instructorRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new BusinessException("Instructor no encontrado"));

        return mapToPerfilDto(instructor);
    }

    public Instructor actualizarInstructorAdmin(Long id, InstructorAdminUpdateDto dto) {
        Instructor instructor = instructorRepository.findActiveById(id)
                .orElseThrow(() -> new BusinessException("Instructor no encontrado o inactivo"));

        instructor.setEspecialidad(dto.getEspecialidad());

        Usuario usuario = instructor.getUsuario();
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());

        usuarioRepository.save(usuario);
        return instructorRepository.save(instructor);
    }

    public Instructor actualizarInstructorSelf(Long id, InstructorSelfUpdateDto dto, String emailActual) {
        Instructor instructor = instructorRepository.findActiveById(id)
                .orElseThrow(() -> new BusinessException("Instructor no encontrado o inactivo"));

        if (!instructor.getUsuario().getEmail().equals(emailActual)) {
            throw new BusinessException("No puedes actualizar otro instructor");
        }

        instructor.setEspecialidad(dto.getEspecialidad());

        Usuario usuario = instructor.getUsuario();
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        usuarioRepository.save(usuario);
        return instructorRepository.save(instructor);
    }


}