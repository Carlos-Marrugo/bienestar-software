package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.dto.RegistroEstudianteDto;
import com.unicolombo.bienestar.exceptions.*;
import com.unicolombo.bienestar.models.Estudiante;
import com.unicolombo.bienestar.models.Role;
import com.unicolombo.bienestar.models.Usuario;
import com.unicolombo.bienestar.repositories.EstudianteRepository;
import com.unicolombo.bienestar.repositories.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class EstudianteService {

    private static final Logger logger = LoggerFactory.getLogger(EstudianteService.class);

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

    /**
     * Registra un nuevo estudiante en el sistema
     * @param dto Datos de registro del estudiante
     * @return Estudiante registrado
     * @throws EmailAlreadyExistsException Si el email ya está registrado
     * @throws CodigoEstudiantilExistsException Si el código estudiantil ya existe
     * @throws InvalidDataException Si los datos no cumplen con las validaciones
     */
    @Transactional(rollbackFor = Exception.class)
    public Estudiante registrarEstudiante(RegistroEstudianteDto dto) {
        try {
            // Validaciones previas
            validateRegistrationData(dto);

            // Crear y guardar usuario
            Usuario usuario = createUserFromDto(dto);
            usuario = usuarioRepo.save(usuario);

            // Crear y guardar estudiante
            Estudiante estudiante = createStudentFromDto(dto, usuario);
            estudiante = estudianteRepo.save(estudiante);

            // Enviar email de bienvenida (no crítico)
            sendWelcomeEmailAsync(usuario, estudiante);

            return estudiante;

        } catch (DataIntegrityViolationException e) {
            logger.error("Error de integridad de datos al registrar estudiante", e);
            throw new DataIntegrityException("Error al guardar los datos del estudiante", e);
        }
    }

    private void validateRegistrationData(RegistroEstudianteDto dto) {
        if (dto == null) {
            throw new InvalidDataException("Los datos de registro no pueden ser nulos");
        }

        if (existeEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException("El email " + dto.getEmail() + " ya está registrado");
        }

        String codigo = dto.getCodigoEstudiantil();
        if (codigo == null || codigo.length() != 10) {
            throw new InvalidDataException("El código estudiantil debe tener exactamente 10 caracteres");
        }

        if (!codigo.matches("\\d+")) {
            throw new InvalidDataException("El código estudiantil solo debe contener números");
        }

        if (existeCodigoEstudiantil(codigo)) {
            throw new CodigoEstudiantilExistsException("El código estudiantil " + codigo + " ya está registrado");
        }
    }

    private Usuario createUserFromDto(RegistroEstudianteDto dto) {
        Usuario usuario = new Usuario();
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setRol(Role.ESTUDIANTE);
        usuario.setActivo(true);
        return usuario;
    }

    private Estudiante createStudentFromDto(RegistroEstudianteDto dto, Usuario usuario) {
        Estudiante estudiante = new Estudiante();
        estudiante.setUsuario(usuario);
        estudiante.setCodigoEstudiantil(dto.getCodigoEstudiantil());
        estudiante.setHorasAcumuladas(0);
        return estudiante;
    }

    private void sendWelcomeEmailAsync(Usuario usuario, Estudiante estudiante) {
        try {
            emailService.sendWelcomeEmail(
                    usuario.getEmail(),
                    usuario.getNombre(),
                    estudiante.getCodigoEstudiantil()
            );
        } catch (Exception e) {
            logger.error("Error al enviar email de bienvenida a " + usuario.getEmail(), e);
        }
    }

    public Optional<Estudiante> obtenerEstudiantePorId(Long id) {
        return estudianteRepo.findById(id);
    }

    public Optional<Estudiante> obtenerEstudiantePorUsuarioId(Long usuarioId) {
        return estudianteRepo.findByUsuarioId(usuarioId);
    }

    public Optional<Estudiante> obtenerEstudiantePorUsuarioEmail(String email) {
        return estudianteRepo.findByUsuarioEmail(email);
    }

    public boolean existeEstudiante(Long id) {
        return estudianteRepo.existsById(id);
    }

    public boolean existeEmail(String email) {
        return usuarioRepo.existsByEmail(email);
    }

    public boolean existeCodigoEstudiantil(String codigoEstudiantil) {
        return estudianteRepo.existsByCodigoEstudiantil(codigoEstudiantil);
    }
}