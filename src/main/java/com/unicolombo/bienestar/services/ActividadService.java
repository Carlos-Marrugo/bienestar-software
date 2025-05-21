package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.dto.ActividadCreateDto;
import com.unicolombo.bienestar.dto.estudiante.EstudianteInscritoDto;
import com.unicolombo.bienestar.exceptions.BusinessException;
import com.unicolombo.bienestar.models.*;
import com.unicolombo.bienestar.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class ActividadService {

    @Autowired
    private ActividadRepository actividadRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AuditoriaService auditoriaService;

    @Autowired
    private UbicacionService ubicacionService;

    @Autowired
    private UbicacionRepository ubicacionRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    public Page<Actividad> listarActividadesAdmin(int page, int size, String filtro) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaInicio").descending());

        if (filtro != null && !filtro.isEmpty()) {
            return actividadRepository.findByNombreContainingIgnoreCase(filtro, pageable);
        }

        return actividadRepository.findAll(pageable);
    }

    public Page<Actividad> findByInstructorId(Long instructorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaInicio").descending());
        return actividadRepository.findByInstructorId(instructorId, pageable);
    }

    @Transactional
    public Actividad crearActividad(ActividadCreateDto dto, String emailUsuario) {
        if (dto.getMaxEstudiantes() == null || dto.getMaxEstudiantes() < 5) {
            throw new BusinessException("La capacidad mínima es de 5 estudiantes");
        }

        Ubicacion ubicacion = ubicacionRepository.findByIdWithHorarios(dto.getUbicacionId())
                .orElseThrow(() -> new BusinessException("Ubicación no encontrada o inactiva"));

        if (dto.getMaxEstudiantes() > ubicacion.getCapacidad()) {
            throw new BusinessException(String.format(
                    "La capacidad (%d) excede el máximo de la ubicación (%d)",
                    dto.getMaxEstudiantes(), ubicacion.getCapacidad()));
        }

        Instructor instructor = instructorRepository.findById(dto.getInstructorId())
                .orElseThrow(() -> new BusinessException("Instructor no encontrado o inactivo"));

        Set<HorarioActividad> horariosEspecificos = new HashSet<>();
        for (ActividadCreateDto.HorarioActividadDto horarioDto : dto.getHorarios()) {
            HorarioUbicacion horarioBase = ubicacion.getHorarios().stream()
                    .filter(h -> h.getId().equals(horarioDto.getHorarioUbicacionId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(
                            "Horario base no encontrado en la ubicación"));

            validarHorarioEspecifico(horarioDto, horarioBase);
            validarSolapamientos(horarioBase, horarioDto, dto, null);

            HorarioActividad horarioActividad = new HorarioActividad();
            horarioActividad.setHorarioBase(horarioBase);
            horarioActividad.setHoraInicio(horarioDto.getHoraInicio());
            horarioActividad.setHoraFin(horarioDto.getHoraFin());
            horarioActividad.setActividad(null);

            horariosEspecificos.add(horarioActividad);
        }

        Actividad actividad = new Actividad();
        actividad.setNombre(dto.getNombre());
        actividad.setFechaInicio(dto.getFechaInicio());
        actividad.setFechaFin(dto.getFechaFin());
        actividad.setMaxEstudiantes(dto.getMaxEstudiantes());
        actividad.setUbicacion(ubicacion);
        actividad.setInstructor(instructor);

        horariosEspecificos.forEach(h -> h.setActividad(actividad));
        actividad.setHorariosEspecificos(horariosEspecificos);

        if (dto.getMaxEstudiantes() > ubicacion.getCapacidad() * 0.9) {
            actividad.addWarning("La actividad está al 90% o más de la capacidad de la ubicación");
        }

        Actividad actividadGuardada = actividadRepository.save(actividad);

        auditoriaService.registrarAccion(
                emailUsuario,
                TipoAccion.CREACION,
                "Actividad creada: " + actividad.getNombre(),
                actividadGuardada.getId()
        );

        return actividadGuardada;
    }

    private void validarHorarioEspecifico(ActividadCreateDto.HorarioActividadDto horarioDto,
                                          HorarioUbicacion horarioBase) {
        if (horarioDto.getHoraInicio().isBefore(horarioBase.getHoraInicio())) {
            throw new BusinessException(String.format(
                    "La hora de inicio (%s) debe ser después del inicio del horario base (%s)",
                    horarioDto.getHoraInicio(), horarioBase.getHoraInicio()));
        }

        if (horarioDto.getHoraFin().isAfter(horarioBase.getHoraFin())) {
            throw new BusinessException(String.format(
                    "La hora de fin (%s) debe ser antes del fin del horario base (%s)",
                    horarioDto.getHoraFin(), horarioBase.getHoraFin()));
        }

        if (Duration.between(horarioDto.getHoraInicio(), horarioDto.getHoraFin()).toMinutes() < 30) {
            throw new BusinessException("La duración mínima de una actividad es de 30 minutos");
        }
    }

    private void validarSolapamientos(HorarioUbicacion horarioBase,
                                      ActividadCreateDto.HorarioActividadDto horarioDto,
                                      ActividadCreateDto dto, Long actividadIdExcluir) {
        if (dto.getFechaInicio().isBefore(LocalDate.now())) {
            throw new BusinessException("No se pueden crear actividades en fechas pasadas");
        }

        if (dto.getFechaFin() != null && dto.getFechaFin().isBefore(dto.getFechaInicio())) {
            throw new BusinessException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }

        boolean existeSolapamiento = actividadRepository.existsSolapamiento(
                horarioBase.getUbicacion().getId(),
                horarioDto.getHoraInicio(),
                horarioDto.getHoraFin(),
                dto.getFechaInicio(),
                dto.getFechaFin(),
                actividadIdExcluir);

        if (existeSolapamiento) {
            throw new BusinessException("El horario seleccionado ya está ocupado por otra actividad");
        }

        boolean instructorOcupado = actividadRepository.existsSolapamientoInstructor(
                dto.getInstructorId(),
                horarioDto.getHoraInicio(),
                horarioDto.getHoraFin(),
                dto.getFechaInicio(),
                dto.getFechaFin(),
                actividadIdExcluir);

        if (instructorOcupado) {
            throw new BusinessException("El instructor ya tiene una actividad programada en ese horario");
        }
    }

    @Transactional
    public Actividad editarActividad(Long id, ActividadCreateDto dto, String emailUsuario) {
        Actividad actividad = actividadRepository.findByIdWithHorarios(id)
                .orElseThrow(() -> new BusinessException("Actividad no encontrada"));

        if (dto.getHorarios() == null || dto.getHorarios().isEmpty()) {
            throw new BusinessException("Debe proporcionar al menos un horario");
        }

        if (!actividad.getInstructor().getId().equals(dto.getInstructorId())) {
            Instructor nuevoInstructor = instructorRepository.findById(dto.getInstructorId())
                    .orElseThrow(() -> new BusinessException("Instructor no encontrado"));

            for (ActividadCreateDto.HorarioActividadDto horarioDto : dto.getHorarios()) {
                HorarioUbicacion horarioBase = ubicacionRepository.findById(dto.getUbicacionId())
                        .orElseThrow(() -> new BusinessException("Ubicación no encontrada"))
                        .getHorarios().stream()
                        .filter(h -> h.getId().equals(horarioDto.getHorarioUbicacionId()))
                        .findFirst()
                        .orElseThrow(() -> new BusinessException("Horario base no encontrado"));

                validarSolapamientos(horarioBase, horarioDto, dto, id);
            }

            actividad.setInstructor(nuevoInstructor);
        }

        actividad.setNombre(dto.getNombre());
        actividad.setFechaInicio(dto.getFechaInicio());
        actividad.setFechaFin(dto.getFechaFin());
        actividad.setMaxEstudiantes(dto.getMaxEstudiantes());

        if (!actividad.getUbicacion().getId().equals(dto.getUbicacionId())) {
            Ubicacion nuevaUbicacion = ubicacionRepository.findById(dto.getUbicacionId())
                    .orElseThrow(() -> new BusinessException("Ubicación no encontrada"));
            actividad.setUbicacion(nuevaUbicacion);
        }

        actividad.getHorariosEspecificos().clear();
        for (ActividadCreateDto.HorarioActividadDto horarioDto : dto.getHorarios()) {
            HorarioUbicacion horarioBase = ubicacionRepository.findById(dto.getUbicacionId())
                    .orElseThrow(() -> new BusinessException("Ubicación no encontrada"))
                    .getHorarios().stream()
                    .filter(h -> h.getId().equals(horarioDto.getHorarioUbicacionId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("Horario base no encontrado en la ubicación"));

            validarHorarioEspecifico(horarioDto, horarioBase);
            validarSolapamientos(horarioBase, horarioDto, dto, id);

            HorarioActividad horarioActividad = new HorarioActividad();
            horarioActividad.setHorarioBase(horarioBase);
            horarioActividad.setHoraInicio(horarioDto.getHoraInicio());
            horarioActividad.setHoraFin(horarioDto.getHoraFin());
            horarioActividad.setActividad(actividad);

            actividad.getHorariosEspecificos().add(horarioActividad);
        }

        auditoriaService.registrarAccion(
                emailUsuario,
                TipoAccion.ACTUALIZACION,
                "Actividad actualizada: " + actividad.getNombre(),
                actividad.getId()
        );

        return actividadRepository.save(actividad);
    }

    @Transactional
    public void eliminarActividad(Long id, String emailUsuario) {
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Actividad no encontrada"));

        LocalDate hoy = LocalDate.now();
        if (actividad.getFechaInicio().isBefore(hoy) ||
                (actividad.getFechaInicio().isEqual(hoy) &&
                        actividad.getHorarioUbicacion().getHoraInicio().isBefore(LocalTime.now()))) {
            throw new BusinessException("No se puede eliminar una actividad que ya ha comenzado");
        }

        /*
        if (!actividad.getInscripciones().isEmpty()) {
            throw new BusinessException("No se puede eliminar una actividad con estudiantes inscritos");
        }*/

        /*if (actividad.getInscripciones().stream()
                .anyMatch(i -> !i.getAsistencias().isEmpty())) {
            throw new BusinessException("No se puede eliminar una actividad con asistencias registradas");
        }*/

        auditoriaService.eliminarRegistrosPorActividad(id);

        actividadRepository.delete(actividad);

        auditoriaService.registrarAccion(
                emailUsuario,
                TipoAccion.ELIMINACION,
                "Actividad eliminada: " + actividad.getNombre(),
                null
        );
    }

    public boolean existeSolapamientoHorario(Long instructorId, LocalDate fecha,
                                             LocalTime horaInicio, LocalTime horaFin,
                                             Long actividadIdExcluir) {
        return actividadRepository.existsByInstructorIdAndFechaInicioAndHoraInicioLessThanAndHoraFinGreaterThanAndIdNot(
                instructorId, fecha, horaFin, horaInicio, actividadIdExcluir);
    }

    public Actividad findById(Long id) {
        Actividad actividad = actividadRepository.findByIdWithHorarios(id)
                .orElseThrow(() -> new BusinessException("Actividad no encontrada"));

        if (actividad.getHorarioUbicacion() == null && !actividad.getHorarios().isEmpty()) {
            actividad.setHorarioUbicacion(actividad.getHorarios().iterator().next());
        }

        return actividad;
    }

    public Page<Actividad> listarActividadesDisponibles(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaInicio").ascending());
        return actividadRepository.findActividadesDisponibles(pageable);
    }

    @Transactional(readOnly = true)
    public Page<EstudianteInscritoDto> getEstudiantesInscritosEnActividad(
            Long actividadId,
            Long instructorId,
            String filtro,
            Pageable pageable) {

        Actividad actividad = actividadRepository.findById(actividadId)
                .orElseThrow(() -> new BusinessException("Actividad no encontrada", HttpStatus.NOT_FOUND));

        if(!actividad.getInstructor().getId().equals(instructorId)) {
            throw new BusinessException("No autorizado para ver esta actividad", HttpStatus.FORBIDDEN);
        }

        Page<Inscripcion> inscripciones = inscripcionRepository.findByActividadId(actividadId, filtro, pageable);

        return inscripciones.map(insc -> new EstudianteInscritoDto(
                insc.getEstudiante().getId(),
                insc.getEstudiante().getCodigoEstudiantil(),
                insc.getEstudiante().getUsuario().getNombre() + " " + insc.getEstudiante().getUsuario().getApellido(),
                insc.getEstudiante().getProgramaAcademico(),
                insc.getEstudiante().getSemestre(),
                insc.getFechaInscripcion().atStartOfDay()
        ));
    }

}