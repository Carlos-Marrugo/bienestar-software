package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.dto.Actividad.UbicacionDto;
import com.unicolombo.bienestar.exceptions.BusinessException;
import com.unicolombo.bienestar.models.HorarioUbicacion;
import com.unicolombo.bienestar.models.TipoAccion;
import com.unicolombo.bienestar.models.Ubicacion;
import com.unicolombo.bienestar.repositories.UbicacionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UbicacionService {

    private final UbicacionRepository ubicacionRepository;
    private final AuditoriaService auditoriaService;

    @Transactional
    public Ubicacion crearUbicacion(UbicacionDto dto, String emailAdmin) {
        if (ubicacionRepository.existsByNombre(dto.getNombre())) {
            throw new BusinessException("Ya existe una ubicación con ese nombre");
        }

        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setNombre(dto.getNombre());
        ubicacion.setCapacidad(dto.getCapacidad());

        // guardar horarios
        dto.getHorarios().forEach(horarioDto -> {
            HorarioUbicacion horario = new HorarioUbicacion();
            horario.setDia(horarioDto.getDia());
            horario.setHoraInicio(horarioDto.getHoraInicio());
            horario.setHoraFin(horarioDto.getHoraFin());
            horario.setUbicacion(ubicacion);
            ubicacion.getHorarios().add(horario);
        });

        auditoriaService.registrarAccion(
                emailAdmin,
                TipoAccion.CREACION,
                "Ubicación creada: " + dto.getNombre()
        );

        return ubicacionRepository.save(ubicacion);
    }

    public List<Ubicacion> listarUbicacionesActivas() {
        return ubicacionRepository.findAllActivas();
    }

    public boolean validarDisponibilidad(
            Long ubicacionId,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin
    ) {
        Ubicacion ubicacion = ubicacionRepository.findByIdAndActivaTrue(ubicacionId)
                .orElseThrow(() -> new BusinessException("Ubicación no disponible o inactiva"));

        if (ubicacionRepository.estaOcupada(ubicacionId, fecha, horaInicio, horaFin)) {
            throw new BusinessException("La ubicación ya está reservada en ese horario");
        }

        DayOfWeek diaActividad = fecha.getDayOfWeek();
        boolean diaValido = ubicacion.getHorarios().stream()
                .anyMatch(horario -> horario.getDia().name().equals(diaActividad.name()));

        if (!diaValido) {
            throw new BusinessException("La ubicación no opera el " + diaActividad);
        }

        boolean horarioValido = ubicacion.getHorarios().stream()
                .filter(horario -> horario.getDia().name().equals(diaActividad.name()))
                .anyMatch(horario ->
                        !horaInicio.isBefore(horario.getHoraInicio()) &&
                                !horaFin.isAfter(horario.getHoraFin())
                );

        if (!horarioValido) {
            throw new BusinessException("El horario no está dentro del rango permitido");
        }

        return true;
    }

    public boolean verificarDisponibilidad(Long ubicacionId, DayOfWeek dia, LocalTime horaInicio, LocalTime horaFin) {
        Ubicacion ubicacion = ubicacionRepository.findByIdAndActivaTrue(ubicacionId)
                .orElseThrow(() -> new BusinessException("Ubicación no disponible o inactiva"));

        boolean diaValido = ubicacion.getHorarios().stream()
                .anyMatch(horario -> horario.getDia().name().equals(dia.name()));

        if (!diaValido) {
            throw new BusinessException("La ubicación no opera el " + dia);
        }

        boolean horarioValido = ubicacion.getHorarios().stream()
                .filter(horario -> horario.getDia().name().equals(dia.name()))
                .anyMatch(horario ->
                        !horaInicio.isBefore(horario.getHoraInicio()) &&
                                !horaFin.isAfter(horario.getHoraFin())
                );

        if (!horarioValido) {
            throw new BusinessException("El horario no está dentro del rango permitido");
        }

        LocalDate fechaEjemplo = LocalDate.now().with(TemporalAdjusters.next(dia));
        if (ubicacionRepository.estaOcupada(ubicacionId, fechaEjemplo, horaInicio, horaFin)) {
            throw new BusinessException("La ubicación ya tiene actividades en ese horario");
        }

        return true;
    }

    public Ubicacion obtenerUbicacion(Long id) {
        return ubicacionRepository.findByIdAndActivaTrue(id)
                .orElseThrow(() -> new BusinessException("Ubicación no encontrada o inactiva"));
    }

    @Transactional
    public Ubicacion actualizarUbicacion(Long id, UbicacionDto dto, String emailAdmin) {
        Ubicacion ubicacion = ubicacionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Ubicación no encontrada"));

        ubicacion.setNombre(dto.getNombre());
        ubicacion.setCapacidad(dto.getCapacidad());

        ubicacion.getHorarios().clear();

        dto.getHorarios().forEach(horarioDto -> {
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
                "Ubicación actualizada: " + dto.getNombre()
        );

        return ubicacionRepository.save(ubicacion);
    }

    @Transactional
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



}