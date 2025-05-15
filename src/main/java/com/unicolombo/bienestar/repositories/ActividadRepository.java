package com.unicolombo.bienestar.repositories;


import com.unicolombo.bienestar.models.Actividad;
import com.unicolombo.bienestar.models.HorarioUbicacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Long> {

    @Query("SELECT a FROM Actividad a " +
            "LEFT JOIN FETCH a.horarioUbicacion h " +
            "LEFT JOIN FETCH a.ubicacion " +
            "LEFT JOIN FETCH a.instructor i " +
            "LEFT JOIN FETCH i.usuario " +
            "WHERE LOWER(a.nombre) LIKE LOWER(concat('%', :filtro,'%'))")
    Page<Actividad> findByNombreContainingIgnoreCase(@Param("filtro") String filtro, Pageable pageable);

    @Query("SELECT a FROM Actividad a JOIN FETCH a.instructor i WHERE i.id = :instructorId")
    Page<Actividad> findByInstructorId(@Param("instructorId") Long instructorId, Pageable pageable);

    @Query("SELECT COUNT(a) > 0 FROM Actividad a WHERE " +
            "a.instructor.id = :instructorId AND " +
            "a.fechaInicio = :fecha AND " +
            "((a.horarioUbicacion.horaInicio < :horaFin AND a.horarioUbicacion.horaFin > :horaInicio) OR " +
            "(a.horarioUbicacion.horaInicio = :horaInicio AND a.horarioUbicacion.horaFin = :horaFin)) AND " +
            "a.id != :actividadIdExcluir")
    boolean existsSolapamientoHorarioExcluyendoActividad(
            @Param("instructorId") Long instructorId,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("actividadIdExcluir") Long actividadIdExcluir);

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
}