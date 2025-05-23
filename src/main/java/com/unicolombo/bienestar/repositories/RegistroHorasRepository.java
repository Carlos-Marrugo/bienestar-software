package com.unicolombo.bienestar.repositories;

import com.unicolombo.bienestar.models.RegistroHoras;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistroHorasRepository extends JpaRepository<RegistroHoras, Long> {
    List<RegistroHoras> findByAsistenciaId(Long asistenciaId);
}