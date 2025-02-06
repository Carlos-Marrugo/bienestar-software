package com.unicolombo.bienestar.repositories;

import com.unicolombo.bienestar.models.Actividad;
import com.unicolombo.bienestar.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ActividadRepository extends JpaRepository<Actividad, Long> {
    List<Actividad> findByInstructorAndFechaInicioBetween(Usuario instructor, LocalDateTime fechaInicio, LocalDateTime fechaFin);
}