package com.unicolombo.bienestar.repositories;

import com.unicolombo.bienestar.models.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    @Query("SELECT a FROM Asistencia a WHERE a.inscripcion.estudiante.id = :estudianteId " +
            "AND a.inscripcion.actividad.id = :actividadId AND a.fecha = :fecha")
    Optional<Asistencia> findByEstudianteIdAndActividadIdAndFecha(
            @Param("estudianteId") Long estudianteId,
            @Param("actividadId") Long actividadId,
            @Param("fecha") LocalDate fecha);

    @Query("SELECT a FROM Asistencia a WHERE a.inscripcion.estudiante.id = :estudianteId " +
            "AND a.inscripcion.actividad.id = :actividadId ORDER BY a.fecha DESC")
    List<Asistencia> findAllByEstudianteIdAndActividadId(
            @Param("estudianteId") Long estudianteId,
            @Param("actividadId") Long actividadId);
}