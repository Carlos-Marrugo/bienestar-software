package com.unicolombo.bienestar.infrastructure.adapters.persistence;

import com.unicolombo.bienestar.domain.model.Usuario;
import com.unicolombo.bienestar.domain.ports.UsuarioRepository;
import com.unicolombo.bienestar.infrastructure.adapters.persistence.entity.UsuarioEntity;
import com.unicolombo.bienestar.infrastructure.adapters.persistence.jpa.JpaUsuarioRepositorySpring;
import com.unicolombo.bienestar.infrastructure.adapters.persistence.mapper.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JpaUsuarioRepository implements UsuarioRepository {
    private final JpaUsuarioRepositorySpring jpaRepository;
    private final UsuarioMapper mapper;

    @Override
    public Usuario save(Usuario usuario) {
        UsuarioEntity entity = mapper.toEntity(usuario);
        UsuarioEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return jpaRepository.findById(id)
                .map(entity -> mapper.toDomain(entity));
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(entity -> mapper.toDomain(entity));
    }

    @Override
    public List<Usuario> findAll() {
        return jpaRepository.findAll().stream()
                .map(entity -> mapper.toDomain(entity))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }
}