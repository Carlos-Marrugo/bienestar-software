package com.unicolombo.bienestar.repositories;

import com.unicolombo.bienestar.models.Actividad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Long> {
    @Query("SELECT a FROM Actividad a WHERE LOWER(a.nombre) LIKE LOWER(concat('%', :filtro,'%'))")
    Page<Actividad> findByNombreContainingIgnoreCase(@Param("filtro") String filtro, Pageable pageable);
}
