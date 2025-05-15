package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.dto.Actividad.HorarioUbicacionDto;
import com.unicolombo.bienestar.dto.Actividad.UbicacionDto;
import com.unicolombo.bienestar.exceptions.BusinessException;
import com.unicolombo.bienestar.models.*;
import com.unicolombo.bienestar.repositories.UbicacionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UbicacionService {

    private final UbicacionRepository ubicacionRepository;
    private final AuditoriaService auditoriaService;

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
        return ubicacionRepository.findAllActivas();
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

    public boolean verificarDisponibilidad(Long ubicacionId, DayOfWeek dia, LocalTime horaInicio, LocalTime horaFin) {
        Ubicacion ubicacion = obtenerUbicacionConHorarios(ubicacionId);
        validarDiaYHorario(ubicacion, dia, horaInicio, horaFin);

        LocalDate fechaEjemplo = LocalDate.now().with(TemporalAdjusters.next(dia));
        if (ubicacionRepository.estaOcupada(ubicacionId, fechaEjemplo, horaInicio, horaFin)) {
            throw new BusinessException("La ubicación ya tiene actividades en ese horario");
        }

        return true;
    }

    @Transactional
    @CacheEvict(value = {"ubicaciones", "ubicacionesActivas", "ubicacionesConHorarios"}, allEntries = true)
    public Ubicacion actualizarHorarios(Long id, List<HorarioUbicacionDto> horariosDto, String emailAdmin) {
        Ubicacion ubicacion = ubicacionRepository.findByIdWithHorarios(id)
                .orElseThrow(() -> new BusinessException("Ubicación no encontrada"));

        List<Long> horariosEnUsoIds = ubicacion.getHorarios().stream()
                .filter(h -> !h.getActividades().isEmpty())
                .map(HorarioUbicacion::getId)
                .collect(Collectors.toList());

        if (!horariosEnUsoIds.isEmpty()) {
            throw new BusinessException(
                    "No se pueden modificar horarios con actividades asociadas",
                    horariosEnUsoIds
            );
        }

        ubicacion.getHorarios().clear();

        horariosDto.forEach(horarioDto -> {
            HorarioUbicacion horario = new HorarioUbicacion();
            horario.setDia(horarioDto.getDia());
            horario.setHoraInicio(horarioDto.getHoraInicio());
            horario.setHoraFin(horarioDto.getHoraFin());
            horario.setFechaInicio(horarioDto.getFechaInicio());
            horario.setFechaFin(horarioDto.getFechaFin());
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

        List<Long> horariosEnUsoIds = ubicacion.getHorarios().stream()
                .filter(h -> !h.getActividades().isEmpty())
                .map(HorarioUbicacion::getId)
                .collect(Collectors.toList());

        if (!horariosEnUsoIds.isEmpty()) {
            throw new BusinessException(
                    "No se pueden modificar horarios con actividades asociadas",
                    horariosEnUsoIds
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
    public void desactivarUbicacion(Long id, String emailAdmin) {
        Ubicacion ubicacion = ubicacionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Ubicación no encontrada"));

        if (!ubicacion.getActividades().isEmpty()) {
            throw new BusinessException("No se puede desactivar una ubicación con actividades asignadas");
        }

        ubicacion.setActiva(false);
        ubicacionRepository.save(ubicacion);

        auditoriaService.registrarAccion(
                emailAdmin,
                TipoAccion.ELIMINACION,
                "Ubicación desactivada: " + ubicacion.getNombre()
        );
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
            throw new BusinessException("Este lugar no esta disponible  " + diaActividad.getNombre());
        }

        boolean horarioValido = ubicacion.getHorarios().stream()
                .filter(horario -> horario.getDia() == diaActividad)
                .anyMatch(horario ->
                        !horaInicio.isBefore(horario.getHoraInicio()) &&
                                !horaFin.isAfter(horario.getHoraFin()) ||
                                (horaInicio.isBefore(horario.getHoraInicio()) &&
                                        horaFin.isAfter(horario.getHoraInicio())) ||
                                (horaInicio.isBefore(horario.getHoraFin()) &&
                                        horaFin.isAfter(horario.getHoraFin()))
                );

        if (!horarioValido) {
            String horariosDisponibles = ubicacion.getHorarios().stream()
                    .filter(horario -> horario.getDia() == diaActividad)
                    .map(horario -> String.format("%s - %s",
                            horario.getHoraInicio(),
                            horario.getHoraFin()))
                    .collect(Collectors.joining(", "));

            throw new BusinessException(
                    String.format("Horario no válido. Horarios disponibles para %s: %s",
                            diaActividad.getNombre(),
                            horariosDisponibles)
            );
        }
    }
}