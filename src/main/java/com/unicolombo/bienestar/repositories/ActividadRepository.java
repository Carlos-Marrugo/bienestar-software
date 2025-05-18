package com.unicolombo.bienestar.repositories;

import com.unicolombo.bienestar.models.Actividad;
import com.unicolombo.bienestar.models.DiaSemana;
import com.unicolombo.bienestar.models.HorarioUbicacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Long> {

    @Query("SELECT a FROM Actividad a " +
            "LEFT JOIN FETCH a.horarioUbicacion h " +
            "LEFT JOIN FETCH h.ubicacion " +
            "LEFT JOIN FETCH a.instructor i " +
            "LEFT JOIN FETCH i.usuario " +
            "WHERE LOWER(a.nombre) LIKE LOWER(concat('%', :filtro,'%'))")
    Page<Actividad> findByNombreContainingIgnoreCase(@Param("filtro") String filtro, Pageable pageable);

    @Query("SELECT a FROM Actividad a JOIN FETCH a.instructor i WHERE i.id = :instructorId")
    Page<Actividad> findByInstructorId(@Param("instructorId") Long instructorId, Pageable pageable);

    @Query("""
    SELECT COUNT(a) > 0 FROM Actividad a
    WHERE a.horarioUbicacion.id = :horarioUbicacionId
      AND (
        (a.fechaInicio BETWEEN :fechaInicio AND :fechaFin)
        OR (a.fechaFin IS NOT NULL AND a.fechaFin BETWEEN :fechaInicio AND :fechaFin)
        OR (a.fechaInicio <= :fechaInicio AND (a.fechaFin IS NULL OR a.fechaFin >= :fechaInicio))
      )
""")
    boolean existsSolapamientoHorario(
            @Param("horarioUbicacionId") Long horarioUbicacionId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );


    @Query("SELECT COUNT(a) > 0 FROM Actividad a WHERE " +
            "a.instructor.id = :instructorId AND " +
            "a.fechaInicio = :fechaInicio AND " +
            "a.horarioUbicacion.horaFin > :horaInicio AND " +
            "a.horarioUbicacion.horaInicio < :horaFin AND " +
            "a.id != :idNot")
    boolean existsByInstructorIdAndFechaInicioAndHoraInicioLessThanAndHoraFinGreaterThanAndIdNot(
            @Param("instructorId") Long instructorId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("idNot") Long idNot);

    @Query("SELECT COUNT(a) > 0 FROM Actividad a WHERE " +
            "a.horarioUbicacion = :horario AND " +
            "(:fechaFin IS NULL OR a.fechaInicio BETWEEN :fechaInicio AND :fechaFin) OR " +
            "(a.fechaFin BETWEEN :fechaInicio AND :fechaFin)")
    boolean existsByHorarioUbicacionAndFechaInicioBetween(
            @Param("horario") HorarioUbicacion horario,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);

    @Query("SELECT a FROM Actividad a JOIN FETCH a.horarioUbicacion WHERE a.id = :id")
    Optional<Actividad> findByIdWithHorario(@Param("id") Long id);

    @Query("""
    SELECT COUNT(a) > 0 
    FROM Actividad a 
    WHERE a.instructor.id = :instructorId
    AND a.horarioUbicacion.id = :horarioId
    AND (
        (:fechaFin IS NULL AND a.fechaInicio = :fechaInicio) OR
        (:fechaFin IS NOT NULL AND (
            (a.fechaInicio >= :fechaInicio AND a.fechaInicio <= :fechaFin) OR
            (a.fechaFin IS NOT NULL AND a.fechaFin >= :fechaInicio AND a.fechaFin <= :fechaFin) OR
            (a.fechaInicio <= :fechaInicio AND (a.fechaFin IS NULL OR a.fechaFin >= :fechaFin))
        ))
    )
    AND (:actividadIdExcluir IS NULL OR a.id != :actividadIdExcluir)
    """)
    boolean existsSolapamientoHorarioExcluyendoActividad(
            @Param("instructorId") Long instructorId,
            @Param("horarioId") Long horarioId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            @Param("actividadIdExcluir") Long actividadIdExcluir
    );


    @Query("""
    SELECT a FROM Actividad a
    WHERE a.horarioUbicacion.id = :horarioId
      AND (
        (a.fechaInicio BETWEEN :fechaInicio AND COALESCE(:fechaFin, a.fechaInicio))
        OR (a.fechaFin IS NOT NULL AND a.fechaFin BETWEEN :fechaInicio AND COALESCE(:fechaFin, a.fechaFin))
        OR (:fechaInicio BETWEEN a.fechaInicio AND COALESCE(a.fechaFin, a.fechaInicio))
      )
      AND (:actividadIdExcluir IS NULL OR a.id != :actividadIdExcluir)
""")
    List<Actividad> findSolapamientos(
            @Param("horarioId") Long horarioId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            @Param("actividadIdExcluir") Long actividadIdExcluir);

    @Query("""
    SELECT a FROM Actividad a
    WHERE a.instructor.id = :instructorId
      AND a.horarioUbicacion.id = :horarioId
      AND (
        (a.fechaInicio BETWEEN :fechaInicio AND COALESCE(:fechaFin, a.fechaInicio))
        OR (a.fechaFin IS NOT NULL AND a.fechaFin BETWEEN :fechaInicio AND COALESCE(:fechaFin, a.fechaFin))
        OR (:fechaInicio BETWEEN a.fechaInicio AND COALESCE(a.fechaFin, a.fechaInicio))
      )
      AND (:actividadIdExcluir IS NULL OR a.id != :actividadIdExcluir)
""")
    List<Actividad> findSolapamientosInstructor(
            @Param("instructorId") Long instructorId,
            @Param("horarioId") Long horarioId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            @Param("actividadIdExcluir") Long actividadIdExcluir);

    @Query("SELECT a FROM Actividad a " +
            "LEFT JOIN FETCH a.horarios h " +
            "LEFT JOIN FETCH h.ubicacion " +
            "LEFT JOIN FETCH a.instructor i " +
            "LEFT JOIN FETCH i.usuario " +
            "WHERE a.id = :id")
    Optional<Actividad> findByIdWithHorarios(@Param("id") Long id);

    @Query("""
    SELECT COUNT(a) > 0 FROM Actividad a
    JOIN a.horariosEspecificos h
    WHERE h.horarioBase.ubicacion.id = :ubicacionId
    AND (
        (a.fechaInicio BETWEEN :fechaInicio AND COALESCE(:fechaFin, a.fechaInicio))
        OR (a.fechaFin IS NOT NULL AND a.fechaFin BETWEEN :fechaInicio AND COALESCE(:fechaFin, a.fechaFin))
        OR (:fechaInicio BETWEEN a.fechaInicio AND COALESCE(a.fechaFin, a.fechaInicio))
    )
    AND (
        (:horaInicio < h.horaFin AND :horaFin > h.horaInicio)
    )
    AND (:actividadIdExcluir IS NULL OR a.id != :actividadIdExcluir)
    """)
    boolean existsSolapamiento(
            @Param("ubicacionId") Long ubicacionId,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            @Param("actividadIdExcluir") Long actividadIdExcluir);

    @Query("""
    SELECT COUNT(a) > 0 FROM Actividad a
    JOIN a.horarios h
    JOIN h.ubicacion u
    WHERE u.id = :ubicacionId
    AND (
        (a.fechaInicio <= :fecha AND (a.fechaFin IS NULL OR a.fechaFin >= :fecha))
    )
    AND (
        (:horaInicio < h.horaFin AND :horaFin > h.horaInicio)
    )
""")
    boolean existsByUbicacionAndFechaAndHorario(
            @Param("ubicacionId") Long ubicacionId,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin);

    @Query("""
    SELECT COUNT(a) > 0 FROM Actividad a
    JOIN a.horarios h
    JOIN h.ubicacion u
    WHERE u.id = :ubicacionId
    AND h.dia = :dia
    AND (
        (:horaInicio < h.horaFin AND :horaFin > h.horaInicio)
    )
""")
    boolean existsByUbicacionAndDiaAndHorario(
            @Param("ubicacionId") Long ubicacionId,
            @Param("dia") DiaSemana dia,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin);

    @Query("""
    SELECT COUNT(a) > 0 FROM Actividad a
    JOIN a.horariosEspecificos h
    WHERE a.instructor.id = :instructorId
    AND (
        (a.fechaInicio BETWEEN :fechaInicio AND COALESCE(:fechaFin, a.fechaInicio))
        OR (a.fechaFin IS NOT NULL AND a.fechaFin BETWEEN :fechaInicio AND COALESCE(:fechaFin, a.fechaFin))
        OR (:fechaInicio BETWEEN a.fechaInicio AND COALESCE(a.fechaFin, a.fechaInicio))
    )
    AND (
        (:horaInicio < h.horaFin AND :horaFin > h.horaInicio)
    )
    AND (:actividadIdExcluir IS NULL OR a.id != :actividadIdExcluir)
    """)
    boolean existsSolapamientoInstructor(
            @Param("instructorId") Long instructorId,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            @Param("actividadIdExcluir") Long actividadIdExcluir);
}
