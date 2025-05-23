package com.unicolombo.bienestar.repositories;

import com.unicolombo.bienestar.dto.Actividad.ActividadEstudianteDto;
import com.unicolombo.bienestar.dto.estudiante.EstudianteInscritoDto;
import com.unicolombo.bienestar.models.Actividad;
import com.unicolombo.bienestar.models.Estudiante;
import com.unicolombo.bienestar.models.Inscripcion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    Optional<Inscripcion> findByEstudianteAndActividad(Estudiante estudiante, Actividad actividad);

    @Query("SELECT i FROM Inscripcion i JOIN FETCH i.actividad a JOIN FETCH a.instructor WHERE i.estudiante.id = :estudianteId")
    Page<Inscripcion> findByEstudianteId(@Param("estudianteId") Long estudianteId, Pageable pageable);

    @Query("SELECT i FROM Inscripcion i JOIN FETCH i.estudiante e JOIN FETCH e.usuario WHERE i.actividad.id = :actividadId")
    Page<Inscripcion> findByActividadId(@Param("actividadId") Long actividadId, Pageable pageable);

    boolean existsByEstudianteIdAndActividadId(Long estudianteId, Long actividadId);

    @Query("SELECT COUNT(i) FROM Inscripcion i WHERE i.actividad.id = :actividadId")
    int countByActividadId(@Param("actividadId") Long actividadId);

    @Query("SELECT i FROM Inscripcion i JOIN FETCH i.actividad a JOIN FETCH i.estudiante e " +
            "JOIN FETCH e.usuario WHERE a.instructor.id = :instructorId")
    Page<Inscripcion> findByInstructorId(@Param("instructorId") Long instructorId, Pageable pageable);

    @Query("SELECT i FROM Inscripcion i JOIN FETCH i.estudiante e JOIN FETCH e.usuario " +
            "WHERE i.actividad.id = :actividadId ORDER BY e.codigoEstudiantil ASC")
    List<Inscripcion> findAllByActividadIdWithEstudiante(@Param("actividadId") Long actividadId);

    @Query("SELECT i FROM Inscripcion i WHERE i.estudiante.id = :estudianteId AND i.actividad.id = :actividadId")
    Optional<Inscripcion> findByEstudianteIdAndActividadId(@Param("estudianteId") Long estudianteId,
                                                           @Param("actividadId") Long actividadId);

    @Query("SELECT i FROM Inscripcion i " +
            "JOIN FETCH i.estudiante e " +
            "JOIN FETCH e.usuario " +
            "WHERE i.actividad.id = :actividadId " +
            "AND (:filtro IS NULL OR " +
            "LOWER(e.usuario.nombre) LIKE LOWER(concat('%', :filtro, '%')) OR " +
            "LOWER(e.usuario.apellido) LIKE LOWER(concat('%', :filtro, '%')) OR " +
            "LOWER(e.codigoEstudiantil) LIKE LOWER(concat('%', :filtro, '%')))")
    Page<Inscripcion> findByActividadId(
            @Param("actividadId") Long actividadId,
            @Param("filtro") String filtro,
            Pageable pageable);

    @Query("SELECT new com.unicolombo.bienestar.dto.estudiante.EstudianteInscritoDto(" +
            "e.id, e.codigoEstudiantil, " +
            "CONCAT(u.nombre, ' ', u.apellido), " +
            "e.programaAcademico, e.semestre, " +
            "i.fechaInscripcion, " +
            "e.horasAcumuladas) " +
            "FROM Inscripcion i " +
            "JOIN i.estudiante e " +
            "JOIN e.usuario u " +
            "WHERE i.actividad.id = :actividadId " +
            "ORDER BY i.fechaInscripcion DESC")
    Page<EstudianteInscritoDto> findEstudiantesInscritosByActividadId(
            @Param("actividadId") Long actividadId,
            Pageable pageable);

    @Query("SELECT new com.unicolombo.bienestar.dto.estudiante.EstudianteInscritoDto(" +
            "e.id, e.codigoEstudiantil, " +
            "CONCAT(u.nombre, ' ', u.apellido), " +
            "e.programaAcademico, e.semestre, " +
            "i.fechaInscripcion, " +
            "e.horasAcumuladas) " +
            "FROM Inscripcion i " +
            "JOIN i.estudiante e " +
            "JOIN e.usuario u " +
            "WHERE i.actividad.id = :actividadId " +
            "AND (LOWER(u.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) " +
            "OR LOWER(u.apellido) LIKE LOWER(CONCAT('%', :filtro, '%')) " +
            "OR LOWER(e.codigoEstudiantil) LIKE LOWER(CONCAT('%', :filtro, '%'))) " +
            "ORDER BY i.fechaInscripcion DESC")
    Page<EstudianteInscritoDto> findEstudiantesInscritosByActividadIdWithFilter(
            @Param("actividadId") Long actividadId,
            @Param("filtro") String filtro,
            Pageable pageable);

    @Query("SELECT new com.unicolombo.bienestar.dto.estudiante.EstudianteInscritoDto(" +
            "e.id, e.codigoEstudiantil, CONCAT(u.nombre, ' ', u.apellido), " +
            "e.programaAcademico, e.semestre, i.fechaInscripcion, e.horasAcumuladas) " +
            "FROM Inscripcion i " +
            "JOIN i.estudiante e " +
            "JOIN e.usuario u " +
            "WHERE i.actividad.instructor.id = :instructorId " +
            "ORDER BY u.nombre ASC, u.apellido ASC")
    Page<EstudianteInscritoDto> findEstudiantesInscritosByInstructorId(
            @Param("instructorId") Long instructorId,
            Pageable pageable);

    //@Query("SELECT i.actividad FROM Inscripcion i WHERE i.estudiante.id = :estudianteId")
    //Page<Actividad> findActividadesByEstudianteId(@Param("estudianteId") Long estudianteId, Pageable pageable);

    @Query("""
        SELECT new com.unicolombo.bienestar.dto.Actividad.ActividadEstudianteDto(
            a.id,
            a.nombre,
            a.fechaInicio,
            a.fechaFin,
            u.nombre
        )
        FROM Inscripcion i
        JOIN i.actividad a
        LEFT JOIN a.ubicacion u
        WHERE i.estudiante.id = :estudianteId
    """)
    Page<ActividadEstudianteDto> findActividadesByEstudianteId(
            @Param("estudianteId") Long estudianteId,
            Pageable pageable
    );
}