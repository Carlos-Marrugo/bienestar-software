package com.unicolombo.bienestar.repositories;


import com.unicolombo.bienestar.models.Actividad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Long> {
    // Método ya proporcionado por JpaRepository
    boolean existsById(Long id);

    // Tus otros métodos existentes...
    @Query("SELECT a FROM Actividad a JOIN FETCH a.instructor i JOIN FETCH i.usuario WHERE LOWER(a.nombre) LIKE LOWER(concat('%', :filtro,'%'))")
    Page<Actividad> findByNombreContainingIgnoreCase(@Param("filtro") String filtro, Pageable pageable);

    @Query("SELECT a FROM Actividad a JOIN FETCH a.instructor i WHERE i.id = :instructorId")
    Page<Actividad> findByInstructorId(@Param("instructorId") Long instructorId, Pageable pageable);

    @Query("""
        SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
        FROM Actividad a
        WHERE a.instructor.id = :instructorId
        AND a.fechaInicio = :fecha
        AND (
            (a.horaInicio < :horaFin AND a.horaFin > :horaInicio)
        )
        """)
    boolean existsSolapamientoHorario(
            @Param("instructorId") Long instructorId,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin);
}