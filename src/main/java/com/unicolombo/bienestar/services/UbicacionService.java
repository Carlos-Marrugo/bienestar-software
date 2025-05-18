package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.dto.Actividad.HorarioUbicacionDto;
import com.unicolombo.bienestar.dto.Actividad.UbicacionDto;
import com.unicolombo.bienestar.exceptions.BusinessException;
import com.unicolombo.bienestar.models.*;
import com.unicolombo.bienestar.repositories.ActividadRepository;
import com.unicolombo.bienestar.repositories.UbicacionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UbicacionService {

    private final UbicacionRepository ubicacionRepository;
    private final AuditoriaService auditoriaService;
    private final ActividadRepository actividadRepository;

    @Transactional
    @CacheEvict(value = {"ubicaciones", "ubicacionesActivas", "ubicacionesConHorarios"}, allEntries = true)
    public Ubicacion crearUbicacion(UbicacionDto dto, String emailAdmin) {
        if (ubicacionRepository.existsByNombre(dto.getNombre())) {
            throw new BusinessException("Ya existe una ubicación con ese nombre");
        }

        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setNombre(dto.getNombre());
        ubicacion.setCapacidad(dto.getCapacidad());

        dto.getHorarios().forEach(horarioDto -> {
            HorarioUbicacion horario = new HorarioUbicacion();
            horario.setDia(horarioDto.getDia());
            horario.setHoraInicio(horarioDto.getHoraInicio());
            horario.setHoraFin(horarioDto.getHoraFin());
            horario.setFechaInicio(horarioDto.getFechaInicio());
            horario.setFechaFin(horarioDto.getFechaFin());
            horario.setUbicacion(ubicacion);
            ubicacion.getHorarios().add(horario);
        });

        auditoriaService.registrarAccion(emailAdmin, TipoAccion.CREACION, "Ubicación creada: " + dto.getNombre());
        return ubicacionRepository.save(ubicacion);
    }

    @Cacheable(value = "ubicacionesActivas")
    public List<Ubicacion> listarUbicacionesActivas() {
        List<Ubicacion> ubicaciones = ubicacionRepository.findAllActivas();
        ubicaciones.forEach(u -> {
            u.getHorarios().forEach(h -> {
                h.setUbicacion(null);
                h.setActividades(null);
            });
        });
        return ubicaciones;
    }

    public boolean validarDisponibilidad(
            Long ubicacionId,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin
    ) {
        Ubicacion ubicacion = obtenerUbicacionConHorarios(ubicacionId);
        DayOfWeek diaSemana = fecha.getDayOfWeek();

        validarDiaYHorario(ubicacion, diaSemana, horaInicio, horaFin);

        if (ubicacionRepository.estaOcupada(ubicacionId, fecha, horaInicio, horaFin)) {
            throw new BusinessException("La ubicación ya está reservada en ese horario");
        }

        return true;
    }

    public boolean verificarDisponibilidad(Long ubicacionId, DiaSemana dia,
                                           LocalTime horaInicio, LocalTime horaFin,
                                           LocalDate fecha) {
        Ubicacion ubicacion = obtenerUbicacionConHorarios(ubicacionId);

        boolean horarioBaseExiste = ubicacion.getHorarios().stream()
                .anyMatch(h -> h.getDia() == dia &&
                        !horaInicio.isBefore(h.getHoraInicio()) &&
                        !horaFin.isAfter(h.getHoraFin()));

        if (!horarioBaseExiste) {
            throw new BusinessException("El horario seleccionado no está dentro de los horarios disponibles");
        }

        boolean ocupado;
        if (fecha != null) {
            ocupado = actividadRepository.existsByUbicacionAndFechaAndHorario(
                    ubicacionId,
                    fecha,
                    horaInicio,
                    horaFin);
        } else {
            ocupado = actividadRepository.existsByUbicacionAndDiaAndHorario(
                    ubicacionId,
                    dia,
                    horaInicio,
                    horaFin);
        }

        if (ocupado) {
            throw new BusinessException("La ubicación ya está reservada en ese horario");
        }

        return true;
    }



    @Cacheable(value = "ubicaciones", key = "#id")
    public Ubicacion obtenerUbicacion(Long id) {
        return ubicacionRepository.findByIdAndActivaTrue(id)
                .orElseThrow(() -> new BusinessException("Ubicación no encontrada o inactiva"));
    }

    @Cacheable(value = "ubicacionesConHorarios", key = "#id")
    public Ubicacion obtenerUbicacionConHorarios(Long id) {
        return ubicacionRepository.findByIdWithHorarios(id)
                .orElseThrow(() -> new BusinessException("Ubicación no encontrada o inactiva"));
    }

    @Transactional
    @CacheEvict(value = {"ubicaciones", "ubicacionesActivas", "ubicacionesConHorarios"}, allEntries = true)
    public Ubicacion actualizarUbicacion(Long id, UbicacionDto dto, String emailAdmin) {
        Ubicacion ubicacion = ubicacionRepository.findByIdWithHorarios(id)
                .orElseThrow(() -> new BusinessException("Ubicación no encontrada"));

        List<Map<String, Object>> conflictos = new ArrayList<>();

        for (HorarioUbicacion h : ubicacion.getHorarios()) {
            if (!h.getActividades().isEmpty()) {
                Map<String, Object> conflicto = new HashMap<>();
                conflicto.put("horarioId", h.getId());

                List<Map<String, Object>> actividadesList = new ArrayList<>();
                for (Actividad a : h.getActividades()) {
                    Map<String, Object> actividadMap = new HashMap<>();
                    actividadMap.put("id", a.getId());
                    actividadMap.put("nombre", a.getNombre());
                    actividadMap.put("fechaInicio", a.getFechaInicio());
                    actividadesList.add(actividadMap);
                }

                conflicto.put("actividades", actividadesList);
                conflictos.add(conflicto);
            }
        }

        if (!conflictos.isEmpty()) {
            throw new BusinessException(
                    "No se pueden modificar horarios con actividades asociadas",
                    conflictos,
                    HttpStatus.CONFLICT
            );
        }

        ubicacion.setNombre(dto.getNombre());
        ubicacion.setCapacidad(dto.getCapacidad());

        ubicacion.getHorarios().clear();
        dto.getHorarios().forEach(horarioDto -> {
            HorarioUbicacion horario = new HorarioUbicacion();
            horario.setDia(horarioDto.getDia());
            horario.setHoraInicio(horarioDto.getHoraInicio());
            horario.setHoraFin(horarioDto.getHoraFin());
            horario.setFechaInicio(horarioDto.getFechaInicio());
            horario.setFechaFin(horarioDto.getFechaFin());
            horario.setUbicacion(ubicacion);
            ubicacion.getHorarios().add(horario);
        });

        auditoriaService.registrarAccion(emailAdmin, TipoAccion.ACTUALIZACION, "Ubicación actualizada: " + dto.getNombre());
        return ubicacionRepository.save(ubicacion);
    }

    @Transactional
    @CacheEvict(value = {"ubicaciones", "ubicacionesActivas", "ubicacionesConHorarios"}, allEntries = true)
    public Ubicacion desactivarUbicacion(Long id, String emailAdmin) {
        Ubicacion ubicacion = ubicacionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Ubicación no encontrada"));

        if (!ubicacion.getActividades().isEmpty()) {
            List<Map<String, Object>> conflictos = ubicacion.getActividades().stream()
                    .map(a -> Map.<String, Object>of(
                            "id", a.getId(),
                            "nombre", a.getNombre(),
                            "fechaInicio", a.getFechaInicio()
                    ))
                    .collect(Collectors.toList());

            throw new BusinessException(
                    "No se puede desactivar una ubicación con actividades asignadas",
                    conflictos,
                    HttpStatus.CONFLICT
            );
        }

        ubicacion.setActiva(false);
        ubicacionRepository.save(ubicacion);

        auditoriaService.registrarAccion(
                emailAdmin,
                TipoAccion.ELIMINACION,
                "Ubicación desactivada: " + ubicacion.getNombre()
        );

        return ubicacion;
    }

    public List<HorarioUbicacion> findHorariosConActividades(Long ubicacionId) {
        Ubicacion ubicacion = ubicacionRepository.findByIdWithHorarios(ubicacionId)
                .orElseThrow(() -> new BusinessException("Ubicación no encontrada"));
        return ubicacion.getHorarios().stream()
                .filter(h -> !h.getActividades().isEmpty())
                .toList();
    }

    private void validarDiaYHorario(Ubicacion ubicacion, DayOfWeek dia, LocalTime horaInicio, LocalTime horaFin) {
        DiaSemana diaActividad = DiaSemana.fromDayOfWeek(dia);

        boolean diaValido = ubicacion.getHorarios().stream()
                .anyMatch(horario -> horario.getDia() == diaActividad);

        if (!diaValido) {
            throw new BusinessException("Este lugar no está disponible los " + diaActividad.getNombre());
        }

        boolean horarioValido = ubicacion.getHorarios().stream()
                .filter(horario -> horario.getDia() == diaActividad)
                .anyMatch(horario ->
                        horaInicio.isAfter(horario.getHoraInicio()) &&
                                horaFin.isBefore(horario.getHoraFin()) &&
                                horaInicio.isBefore(horaFin)
                );

        if (!horarioValido) {
            String horariosDisponibles = ubicacion.getHorarios().stream()
                    .filter(horario -> horario.getDia() == diaActividad)
                    .map(horario -> String.format("%s - %s",
                            horario.getHoraInicio(),
                            horario.getHoraFin()))
                    .collect(Collectors.joining(", "));

            throw new BusinessException(
                    String.format("Horario no válido. Debe estar dentro de los rangos disponibles para %s: %s",
                            diaActividad.getNombre(),
                            horariosDisponibles)
            );
        }
    }

    @Transactional
    public Ubicacion cambiarEstadoUbicacion(Long id, boolean activa, String emailAdmin) {
        Ubicacion ubicacion = ubicacionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Ubicación no encontrada", HttpStatus.NOT_FOUND));

        if (!activa) {
            List<Map<String, Object>> conflictos = new ArrayList<>();

            for (HorarioUbicacion h : ubicacion.getHorarios()) {
                for (Actividad a : h.getActividades()) {
                    Map<String, Object> conflicto = new HashMap<>();
                    conflicto.put("actividadId", a.getId());
                    conflicto.put("nombre", a.getNombre());
                    conflicto.put("fechaInicio", a.getFechaInicio());
                    conflictos.add(conflicto);
                }
            }

            if (!conflictos.isEmpty()) {
                throw new BusinessException(
                        "No se puede desactivar una ubicación con actividades asignadas",
                        conflictos,
                        HttpStatus.CONFLICT
                );
            }
        }

        ubicacion.setActiva(activa);
        ubicacion = ubicacionRepository.save(ubicacion);

        auditoriaService.registrarAccion(
                emailAdmin,
                activa ? TipoAccion.ACTIVACION : TipoAccion.DESACTIVACION,
                "Ubicación " + (activa ? "activada" : "desactivada") + ": " + ubicacion.getNombre(),
                ubicacion.getId()
        );

        return ubicacion;
    }

    public Map<String, Object> verificarDisponibilidad(
            Long ubicacionId,
            DayOfWeek dia,
            LocalTime horaInicio,
            LocalTime horaFin,
            LocalDate fecha) {

        Ubicacion ubicacion = obtenerUbicacionConHorarios(ubicacionId);
        DiaSemana diaActividad = DiaSemana.fromDayOfWeek(dia);

        List<HorarioUbicacion> horariosDisponibles = ubicacion.getHorarios().stream()
                .filter(h -> h.getDia() == diaActividad)
                .collect(Collectors.toList());

        if (horariosDisponibles.isEmpty()) {
            throw new BusinessException("La ubicación no está disponible este día");
        }

        boolean horarioValido = horariosDisponibles.stream()
                .anyMatch(h ->
                        !horaInicio.isBefore(h.getHoraInicio()) &&
                                !horaFin.isAfter(h.getHoraFin()));

        if (!horarioValido) {
            String rangos = horariosDisponibles.stream()
                    .map(h -> h.getHoraInicio() + " - " + h.getHoraFin())
                    .collect(Collectors.joining(", "));

            throw new BusinessException(
                    "Horario no válido. Rangos disponibles: " + rangos
            );
        }

        if (fecha != null) {
            boolean ocupada = ubicacionRepository.estaOcupada(
                    ubicacionId,
                    fecha,
                    horaInicio,
                    horaFin
            );

            if (ocupada) {
                throw new BusinessException("La ubicación ya está reservada para esta fecha y horario");
            }
        }

        return Map.of(
                "disponible", true,
                "ubicacion", Map.of(
                        "id", ubicacion.getId(),
                        "nombre", ubicacion.getNombre(),
                        "capacidad", ubicacion.getCapacidad()
                ),
                "horariosDisponibles", horariosDisponibles.stream()
                        .map(h -> Map.of(
                                "id", h.getId(),
                                "dia", h.getDia(),
                                "horaInicio", h.getHoraInicio(),
                                "horaFin", h.getHoraFin()
                        ))
                        .collect(Collectors.toList())
        );
    }


    @Transactional
    @CacheEvict(value = {"ubicaciones", "ubicacionesActivas", "ubicacionesConHorarios"}, allEntries = true)
    public Ubicacion actualizarHorarios(Long id, List<HorarioUbicacionDto> horariosDto, String emailAdmin) {
        Ubicacion ubicacion = ubicacionRepository.findByIdWithHorarios(id)
                .orElseThrow(() -> new BusinessException("Ubicación no encontrada"));

        List<Map<String, Object>> conflictos = new ArrayList<>();

        for (HorarioUbicacion h : ubicacion.getHorarios()) {
            if (!h.getActividades().isEmpty()) {
                Map<String, Object> conflicto = new HashMap<>();
                conflicto.put("horarioId", h.getId());

                List<Map<String, Object>> actividadesList = new ArrayList<>();
                for (Actividad a : h.getActividades()) {
                    Map<String, Object> actividadMap = new HashMap<>();
                    actividadMap.put("id", a.getId());
                    actividadMap.put("nombre", a.getNombre());
                    actividadesList.add(actividadMap);
                }

                conflicto.put("actividades", actividadesList);
                conflictos.add(conflicto);
            }
        }

        if (!conflictos.isEmpty()) {
            throw new BusinessException(
                    "No se pueden modificar horarios con actividades asociadas",
                    conflictos,
                    HttpStatus.CONFLICT
            );
        }

        ubicacion.getHorarios().clear();
        horariosDto.forEach(horarioDto -> {
            HorarioUbicacion horario = new HorarioUbicacion();
            horario.setDia(horarioDto.getDia());
            horario.setHoraInicio(horarioDto.getHoraInicio());
            horario.setHoraFin(horarioDto.getHoraFin());
            horario.setUbicacion(ubicacion);
            ubicacion.getHorarios().add(horario);
        });

        auditoriaService.registrarAccion(
                emailAdmin,
                TipoAccion.ACTUALIZACION,
                "Horarios de ubicación actualizados"
        );

        return ubicacionRepository.save(ubicacion);
    }



}