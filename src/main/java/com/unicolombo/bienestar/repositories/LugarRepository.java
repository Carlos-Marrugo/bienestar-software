package com.unicolombo.bienestar.repositories;

import com.unicolombo.bienestar.models.Lugar;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LugarRepository extends JpaRepository<Lugar, Long> {
}