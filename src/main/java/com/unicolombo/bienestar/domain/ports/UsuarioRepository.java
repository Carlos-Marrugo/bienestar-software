package com.unicolombo.bienestar.domain.ports;

import com.unicolombo.bienestar.domain.model.Usuario;
import java.util.Optional;
import java.util.List;

public interface UsuarioRepository {
    Usuario save(Usuario usuario);
    Optional<Usuario> findById(Long id);
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findAll();
    void deleteById(Long id);
    boolean existsByEmail(String email);
}