package com.unicolombo.bienestar.repositories;

import com.unicolombo.bienestar.models.Horario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HorarioRepository extends JpaRepository<Horario, Long> {
}