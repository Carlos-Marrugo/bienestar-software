package com.unicolombo.bienestar.repositories;

import com.unicolombo.bienestar.models.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {
}