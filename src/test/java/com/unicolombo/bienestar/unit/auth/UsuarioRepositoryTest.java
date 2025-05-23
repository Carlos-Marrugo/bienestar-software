package com.unicolombo.bienestar.unit.auth;

import com.unicolombo.bienestar.models.Usuario;
import com.unicolombo.bienestar.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UsuarioRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void findByEmail_WhenUserExists_ReturnsUser() {
        Usuario usuario = new Usuario();
        usuario.setEmail("test@unicolombo.edu.co");
        entityManager.persist(usuario);

        Optional<Usuario> found = usuarioRepository.findByEmail("test@unicolombo.edu.co");

        assertTrue(found.isPresent());
        assertEquals("test@unicolombo.edu.co", found.get().getEmail());
    }
}