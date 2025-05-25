package com.unicolombo.bienestar.repositories;

import com.unicolombo.bienestar.models.HorarioUbicacion;
import com.unicolombo.bienestar.models.Ubicacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface UbicacionRepository extends JpaRepository<Ubicacion, Long> {
    boolean existsByNombre(String nombre);
    Optional<Ubicacion> findByNombre(String nombre);

    @Query("SELECT u FROM Ubicacion u WHERE u.activa = true")
    Page<Ubicacion> findAllActivas(Pageable pageable);

    @Query("SELECT u FROM Ubicacion u WHERE u.id = :id AND u.activa = true")
    Optional<Ubicacion> findByIdAndActivaTrue(@Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
            "FROM Actividad a WHERE " +
            "a.horarioUbicacion.ubicacion.id = :ubicacionId AND " +
            "a.fechaInicio = :fecha AND " +
            "((a.horarioUbicacion.horaInicio < :horaFin AND a.horarioUbicacion.horaFin > :horaInicio))")
    boolean estaOcupada(
            @Param("ubicacionId") Long ubicacionId,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin);

    @Query("SELECT u FROM Ubicacion u LEFT JOIN FETCH u.horarios WHERE u.id = :id")
    Optional<Ubicacion> findByIdWithHorarios(@Param("id") Long id);

    @Query("SELECT h FROM HorarioUbicacion h WHERE h.id = :id")
    Optional<HorarioUbicacion> findHorarioById(@Param("id") Long id);
}