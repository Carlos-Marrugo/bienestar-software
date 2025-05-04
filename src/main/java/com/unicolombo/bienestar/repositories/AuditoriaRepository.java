package com.unicolombo.bienestar.repositories;

import com.unicolombo.bienestar.models.AuditoriaActividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaRepository extends JpaRepository<AuditoriaActividad, Long> {
}
