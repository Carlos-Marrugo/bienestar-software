package com.unicolombo.bienestar.repositories;

import com.unicolombo.bienestar.models.RegistroHoras;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface RegistroHorasRepository extends JpaRepository<RegistroHoras, Long> {

    @Query("SELECT SUM(r.horas) FROM RegistroHoras r WHERE r.estudiante.id = :estudianteId AND r.actividad.id = :actividadId")
    Optional<Integer> sumarHorasPorEstudianteYActividad(
            @Param("estudianteId") Long estudianteId,
            @Param("actividadId") Long actividadId
    );
}
