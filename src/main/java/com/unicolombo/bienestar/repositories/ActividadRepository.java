package com.unicolombo.bienestar.repositories;

import com.unicolombo.bienestar.models.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActividadRepository extends JpaRepository<Actividad, Long> {
}