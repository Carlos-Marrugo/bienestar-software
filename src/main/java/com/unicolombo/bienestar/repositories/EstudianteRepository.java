package com.unicolombo.bienestar.repositories;

import com.unicolombo.bienestar.dto.estudiante.HorasActividadDto;
import com.unicolombo.bienestar.models.EstadoEstudiante;
import com.unicolombo.bienestar.models.Estudiante;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {

    Optional<Estudiante> findByCodigoEstudiantil(String codigo);
    boolean existsByCodigoEstudiantil(String codigo);

    @Query("SELECT e FROM Estudiante e JOIN FETCH e.usuario u WHERE e.estado = 'ACTIVO' AND " +
            "(LOWER(u.nombre) LIKE LOWER(concat('%', :filtro, '%')) OR " +
            "LOWER(u.apellido) LIKE LOWER(concat('%', :filtro, '%')) OR " +
            "LOWER(e.codigoEstudiantil) LIKE LOWER(concat('%', :filtro, '%')))")
    Page<Estudiante> findByFiltro(@Param("filtro") String filtro, Pageable pageable);

    @Query("SELECT e FROM Estudiante e JOIN FETCH e.usuario WHERE e.estado = 'ACTIVO'")
    List<Estudiante> findAllActivos();


    //sql para filtrar por estado
    @Query("SELECT e FROM Estudiante e WHERE e.estado = :estado")
    Page<Estudiante> findByEstado(@Param("estado") EstadoEstudiante estado, Pageable pageable);

    @Query("SELECT e FROM Estudiante e WHERE e.estado = :estado AND " +
            "(LOWER(e.usuario.nombre) LIKE LOWER(concat('%', :filtro, '%')) OR " +
            "LOWER(e.usuario.apellido) LIKE LOWER(concat('%', :filtro, '%')) OR " +
            "LOWER(e.codigoEstudiantil) LIKE LOWER(concat('%', :filtro, '%')))")
    Page<Estudiante> findByEstadoAndFiltro(
            @Param("estado") EstadoEstudiante estado,
            @Param("filtro") String filtro,
            Pageable pageable
    );
}