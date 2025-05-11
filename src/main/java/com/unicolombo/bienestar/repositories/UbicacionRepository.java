package com.unicolombo.bienestar.repositories;

import com.unicolombo.bienestar.models.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface UbicacionRepository extends JpaRepository<Ubicacion, Long> {
    Optional<Ubicacion> findByNombre(String nombre);

    @Query("SELECT u FROM Ubicacion u WHERE u.activa = true")
    List<Ubicacion> findAllActivas();

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
            "FROM Actividad a WHERE a.ubicacion.id = :ubicacionId " +
            "AND a.fechaInicio = :fecha " +
            "AND ((a.horaInicio < :horaFin AND a.horaFin > :horaInicio))")
    boolean estaOcupada(
            @Param("ubicacionId") Long ubicacionId,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin
    );
}