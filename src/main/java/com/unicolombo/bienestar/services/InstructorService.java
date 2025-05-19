package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.dto.Actividad.ActividadInstructorDto;
import com.unicolombo.bienestar.dto.RegistroInstructorDto;
import com.unicolombo.bienestar.dto.InstructorUpdateDto;
import com.unicolombo.bienestar.models.*;
import com.unicolombo.bienestar.repositories.*;
import com.unicolombo.bienestar.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
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
        ZoneId zonaColombia = ZoneId.of("America/Bogota");
        instructor.setFechaContratacion(LocalDate.now(zonaColombia));

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

    public List<Instructor> listarInstructoresActivos() {
        return instructorRepository.findAllActive();
    }

    public Optional<Instructor> obtenerInstructorActivo(Long id) {
        return instructorRepository.findActiveById(id);
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

    public Long getInstructorIdByEmail(String email) throws BusinessException {
        return instructorRepository.findIdByUsuarioEmail(email)
                .orElseThrow(() -> new BusinessException("No se encontró un instructor con ese email"));
    }
}