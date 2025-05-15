package com.unicolombo.bienestar.repositories;

import com.unicolombo.bienestar.models.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {
    Optional<Estudiante> findByUsuarioId(Long usuarioId);
    Optional<Estudiante> findByUsuarioEmail(String email);
    boolean existsByCodigoEstudiantil(String codigoEstudiantil);
}