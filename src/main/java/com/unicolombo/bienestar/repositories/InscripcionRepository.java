package com.unicolombo.bienestar.repositories;

import com.unicolombo.bienestar.models.Estudiante;
import com.unicolombo.bienestar.models.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    @Query("SELECT i.estudiante FROM Inscripcion i " +
            "WHERE i.actividad.id = :actividadId AND i.actividad.instructor.id = :instructorId")
    List<Estudiante> findEstudiantesByActividadAndInstructor(Long actividadId, Long instructorId);

    @Query("SELECT COUNT(i) FROM Inscripcion i WHERE i.actividad.id = :actividadId")
    Long countInscritosByActividadId(Long actividadId);

    boolean existsByActividadIdAndEstudianteId(Long actividadId, Long estudianteId);
}
