package com.unicolombo.bienestar.services;


import com.unicolombo.bienestar.dto.request.actividad.*;
import com.unicolombo.bienestar.dto.request.estudiante.EstudianteInscritoDto;
import com.unicolombo.bienestar.exceptions.BusinessException;
import com.unicolombo.bienestar.exceptions.ResourceNoFoundException;
import com.unicolombo.bienestar.models.*;
import com.unicolombo.bienestar.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    public Page<Actividad> listarActividadesAdmin(int page, int size, String filtro, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaInicio").descending());

        if (search != null && !search.isEmpty()) {
            return actividadRepository.findByNombreContainingIgnoreCase(search, pageable);
        }
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
    /*public Actividad editarActividad(Long id, ActividadUpdateDto dto, String emailUsuario) {
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Actividad no encontrada"));

        if (dto.getFechaInicio().isBefore(LocalDate.now())) {
            throw new BusinessException("No se puede mover la actividad al pasado");
        }

        if (dto.getFechaFin() != null && dto.getFechaFin().isBefore(dto.getFechaInicio())) {
            throw new BusinessException("La fecha de fin debe ser posterior a la de inicio");
        }

        actividad.setNombre(dto.getNombre());
        actividad.setFechaInicio(dto.getFechaInicio());
        actividad.setFechaFin(dto.getFechaFin());
        actividad.setMaxEstudiantes(dto.getMaxEstudiantes());

        if (!actividad.getInstructor().getId().equals(dto.getInstructorId())) {
            Instructor nuevoInstructor = instructorRepository.findById(dto.getInstructorId())
                    .orElseThrow(() -> new BusinessException("Instructor no encontrado"));
            actividad.setInstructor(nuevoInstructor);
        }

        auditoriaService.registrarAccion(
                emailUsuario,
                TipoAccion.ACTUALIZACION,
                "Actividad actualizada: " + actividad.getNombre(),
                actividad.getId()
        );

        return actividadRepository.save(actividad);
    }*/
    public Actividad editarActividad(Long id, ActividadUpdateDto dto, String emailUsuario) {
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Actividad no encontrada"));

        boolean isStart = actividad.getFechaInicio().isBefore(LocalDate.now());

        if (!isStart && dto.getFechaInicio().isBefore(LocalDate.now())) {
            throw new BusinessException("No se puede mover la actividad al pasado");
        }

        if (dto.getFechaFin() != null && dto.getFechaFin().isBefore(dto.getFechaInicio())) {
            throw new BusinessException("La fecha de fin debe ser posterior a la de inicio");
        }

        actividad.setNombre(dto.getNombre());

        if (isStart) {
            if (actividad.getUbicacion().getId() != dto.getUbicacion()){
                throw new BusinessException("No se puede modificar la ubicacion de una actividad iniciada");
            }
            if (dto.getFechaFin() != null) {
                actividad.setFechaFin(dto.getFechaFin());
            }
        } else {
            actividad.setFechaInicio(dto.getFechaInicio());
            actividad.setFechaFin(dto.getFechaFin());
            actividad.setMaxEstudiantes(dto.getMaxEstudiantes());
            Ubicacion ubicacion = ubicacionRepository.findByIdWithHorarios(dto.getUbicacion()).
                    orElseThrow(() -> new BusinessException("Ubicacion no encontrada"));
            actividad.setUbicacion(ubicacion);
            if (!actividad.getInstructor().getId().equals(dto.getInstructorId())) {
                Instructor nuevoInstructor = instructorRepository.findById(dto.getInstructorId())
                        .orElseThrow(() -> new BusinessException("Instructor no encontrado"));
                actividad.setInstructor(nuevoInstructor);
            }

            if (dto.getHorarios() != null && !dto.getHorarios().isEmpty()) {
                Set<HorarioActividad> nuevosHorarios = new HashSet<>();

                for (ActividadCreateDto.HorarioActividadDto horarioDto : dto.getHorarios()) {
                    HorarioActividad nuevo = getHorarioActividad(horarioDto, actividad);
                    nuevosHorarios.add(nuevo);
                }

                actividad.getHorariosEspecificos().clear();
                actividad.getHorariosEspecificos().addAll(nuevosHorarios);
            }
        }

        auditoriaService.registrarAccion(
                emailUsuario,
                TipoAccion.ACTUALIZACION,
                "Actividad actualizada: " + actividad.getNombre(),
                actividad.getId()
        );

        return actividadRepository.save(actividad);
    }

    private static HorarioActividad getHorarioActividad(ActividadCreateDto.HorarioActividadDto horarioDto, Actividad actividad) {
        HorarioUbicacion base = actividad.getHorarioUbicacion();

        if (horarioDto.getHoraInicio().isBefore(base.getHoraInicio()) ||
                horarioDto.getHoraFin().isAfter(base.getHoraFin())) {
            throw new BusinessException("El horario específico [" + horarioDto.getHoraInicio() + " - " + horarioDto.getHoraFin() +
                    "] está fuera del rango permitido por el horario base (" +
                    base.getHoraInicio() + " - " + base.getHoraFin() + ")");
        }

        HorarioActividad nuevo = new HorarioActividad();
        nuevo.setHorarioBase(base);
        nuevo.setHoraInicio(horarioDto.getHoraInicio());
        nuevo.setHoraFin(horarioDto.getHoraFin());
        nuevo.setActividad(actividad);
        return nuevo;
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

    @Cacheable(value = "actividadesDisponibles", key = "{#pageable.pageNumber, #pageable.pageSize, #pageable.sort}")
    public Page<ActividadDisponibleSimpleDto> obtenerActividadesDisponiblesSimples(Pageable pageable) {
        LocalDate hoy = LocalDate.now();
        Page<Actividad> actividadesPage = actividadRepository.findByFechaFinGreaterThanEqualAndUbicacionIsNotNull(hoy, pageable);

        return actividadesPage.map(actividad -> {
            int inscritos = inscripcionRepository.countByActividadId(actividad.getId());
            return new ActividadDisponibleSimpleDto(actividad, inscritos);
        });
    }

    @Cacheable(value = "actividadesDisponiblesSimplesSearch", key = "{#page, #size, #search}")
    public Page<ActividadDisponibleSimpleDto> buscarActividadesDisponiblesSimples(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaInicio").ascending());

        LocalDate hoy = LocalDate.now();
        Page<Actividad> actividadesPage;

        if (search != null && !search.isEmpty()) {
            actividadesPage = actividadRepository
                    .findByFechaFinGreaterThanEqualAndUbicacionIsNotNullAndNombreContainingIgnoreCase(hoy, search, pageable);
        } else {
            actividadesPage = actividadRepository
                    .findByFechaFinGreaterThanEqualAndUbicacionIsNotNull(hoy, pageable);
        }

        return actividadesPage.map(actividad -> {
            int inscritos = inscripcionRepository.countByActividadId(actividad.getId());
            return new ActividadDisponibleSimpleDto(actividad, inscritos);
        });
    }

    @Transactional(readOnly = true)
    public Page<EstudianteInscritoDto> getEstudiantesInscritosEnActividad(
            Long actividadId,
            Long instructorId,
            String filtro,
            Pageable pageable) {

        Actividad actividad = actividadRepository.findById(actividadId)
                .orElseThrow(() -> new BusinessException("Actividad no encontrada"));

        if (instructorId != null && !actividad.getInstructor().getId().equals(instructorId)) {
            throw new BusinessException("No tienes permisos para ver los estudiantes de esta actividad", HttpStatus.FORBIDDEN);
        }

        String terminoBusqueda = (filtro != null && !filtro.trim().isEmpty())
                ? "%" + filtro.trim().toLowerCase() + "%"
                : null;

        if (terminoBusqueda != null) {
            return inscripcionRepository.findEstudiantesInscritosByActividadIdWithFilter(
                    actividadId, terminoBusqueda, pageable);
        } else {
            return inscripcionRepository.findEstudiantesInscritosByActividadId(
                    actividadId, pageable);
        }
    }

    @Cacheable(value = "actividadesInscritas", key = "{#estudianteId, #pageable.pageNumber, #pageable.pageSize, #search}")
    public Page<ActividadEstudianteDto> obtenerActividadesInscritasPorEstudiante(
            Long estudianteId,
            String search,
            Pageable pageable) {

        Page<Actividad> actividades;

        if (search != null && !search.trim().isEmpty()) {
            actividades = actividadRepository.findActividadesByEstudianteIdAndSearch(
                    estudianteId,
                    search.trim().toLowerCase(),
                    pageable);
        } else {
            actividades = actividadRepository.findActividadesByEstudianteId(estudianteId, pageable);
        }

        if (actividades.isEmpty()) {
            throw new BusinessException("No se encontraron actividades para este estudiante");
        }

        return actividades.map(actividad -> {
            ActividadEstudianteDto dto = new ActividadEstudianteDto(actividad);
            return dto;
        });
    }

}