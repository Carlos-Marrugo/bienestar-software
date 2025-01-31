package com.unicolombo.bienestar.infrastructure.adapters.persistence.jpa;

import com.unicolombo.bienestar.infrastructure.adapters.persistence.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaUsuarioRepositorySpring extends JpaRepository<UsuarioEntity, Long> {
    Optional<UsuarioEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}