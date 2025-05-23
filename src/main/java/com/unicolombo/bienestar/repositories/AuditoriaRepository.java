package com.unicolombo.bienestar.repositories;

import com.unicolombo.bienestar.models.AuditoriaActividad;
import com.unicolombo.bienestar.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuditoriaRepository extends JpaRepository<AuditoriaActividad, Long> {
    @Query("SELECT a FROM AuditoriaActividad a ORDER BY a.fecha DESC LIMIT 5")
    List<AuditoriaActividad> findTop5ByOrderByFechaDesc();

    Optional<AuditoriaActividad> findByToken(String token);

    @Modifying
    @Query("DELETE FROM AuditoriaActividad a WHERE a.usuario = :usuario")
    void deleteByUsuario(Usuario usuario);

    @Modifying
    @Query("DELETE FROM AuditoriaActividad a WHERE a.expiryDate < :now")
    void deleteAllByExpiryDateBefore(Instant now);

    @Modifying
    @Query("DELETE FROM AuditoriaActividad a WHERE a.actividad.id = :actividadId")
    void deleteByActividadId(@Param("actividadId") Long actividadId);

    @Query("SELECT COUNT(a) > 0 FROM Actividad a WHERE a.id = :actividadId")
    boolean existsActividadById(@Param("actividadId") Long actividadId);
}