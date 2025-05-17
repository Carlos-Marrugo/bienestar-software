package com.unicolombo.bienestar.repositories;

import com.unicolombo.bienestar.models.Actividad;
import com.unicolombo.bienestar.models.Estudiante;
import com.unicolombo.bienestar.models.Inscripcion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    // Buscar inscripción por estudiante y actividad
    Optional<Inscripcion> findByEstudianteAndActividad(Estudiante estudiante, Actividad actividad);

    // Listar inscripciones por estudiante
    @Query("SELECT i FROM Inscripcion i JOIN FETCH i.actividad a JOIN FETCH a.instructor WHERE i.estudiante.id = :estudianteId")
    Page<Inscripcion> findByEstudianteId(@Param("estudianteId") Long estudianteId, Pageable pageable);

    // Listar inscripciones por actividad
    @Query("SELECT i FROM Inscripcion i JOIN FETCH i.estudiante e JOIN FETCH e.usuario WHERE i.actividad.id = :actividadId")
    Page<Inscripcion> findByActividadId(@Param("actividadId") Long actividadId, Pageable pageable);

    // Verificar si un estudiante está inscrito en una actividad
    boolean existsByEstudianteIdAndActividadId(Long estudianteId, Long actividadId);

    // Contar inscripciones por actividad
    @Query("SELECT COUNT(i) FROM Inscripcion i WHERE i.actividad.id = :actividadId")
    int countByActividadId(@Param("actividadId") Long actividadId);

    // Listar inscripciones por instructor
    @Query("SELECT i FROM Inscripcion i JOIN FETCH i.actividad a JOIN FETCH i.estudiante e " +
            "JOIN FETCH e.usuario WHERE a.instructor.id = :instructorId")
    Page<Inscripcion> findByInstructorId(@Param("instructorId") Long instructorId, Pageable pageable);

    // Buscar inscripciones por actividad y con fetch para estudiante y usuario
    @Query("SELECT i FROM Inscripcion i JOIN FETCH i.estudiante e JOIN FETCH e.usuario " +
            "WHERE i.actividad.id = :actividadId")
    List<Inscripcion> findAllByActividadIdWithEstudiante(@Param("actividadId") Long actividadId);
}