package com.unicolombo.bienestar.repositories;

import com.unicolombo.bienestar.dto.request.actividad.ActividadInstructorDto;
import com.unicolombo.bienestar.models.Actividad;
import com.unicolombo.bienestar.models.Instructor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Repository
public interface InstructorRepository extends JpaRepository<Instructor, Long> {
    @EntityGraph(attributePaths = {"usuario"})
    @Query("SELECT i FROM Instructor i WHERE i.usuario.activo = true")
    Page<Instructor> findAllActive(Pageable pageable);

    @Query("SELECT i FROM Instructor i JOIN FETCH i.usuario u WHERE i.id = :id AND u.activo = true")
    Optional<Instructor> findActiveById(@Param("id") Long id);

    @Query("SELECT i FROM Instructor i JOIN FETCH i.usuario WHERE i.usuario.email = :email")
    Optional<Instructor> findByUsuarioEmail(@Param("email") String email);

    @Query("SELECT i.id FROM Instructor i WHERE i.usuario.email = :email")
    Optional<Long> findIdByUsuarioEmail(@Param("email") String email);
}