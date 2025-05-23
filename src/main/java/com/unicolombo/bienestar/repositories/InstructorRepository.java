package com.unicolombo.bienestar.repositories;

import com.unicolombo.bienestar.models.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstructorRepository extends JpaRepository<Instructor, Long> {
    @Query("SELECT i FROM Instructor i JOIN FETCH i.usuario WHERE i.usuario.activo = true")
    List<Instructor> findAllActive();

    @Query("SELECT i FROM Instructor i JOIN FETCH i.usuario u WHERE i.id = :id AND u.activo = true")
    Optional<Instructor> findActiveById(@Param("id") Long id);

    @Query("SELECT i FROM Instructor i JOIN FETCH i.usuario WHERE i.usuario.email = :email")
    Optional<Instructor> findByUsuarioEmail(@Param("email") String email);

    @Query("SELECT i FROM Instructor i JOIN FETCH i.actividades WHERE i.id = :instructorId")
    Optional<Instructor> findByIdWithActividades(@Param("instructorId") Long instructorId);

    @Query("SELECT i.id FROM Instructor i WHERE i.usuario.email = :email")
    Optional<Long> findIdByUsuarioEmail(@Param("email") String email);
}