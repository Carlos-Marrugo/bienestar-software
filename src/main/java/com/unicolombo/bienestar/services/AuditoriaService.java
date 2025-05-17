package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.dto.ActividadCreateDto;
import com.unicolombo.bienestar.dto.AuditoriaCreateDto;
import com.unicolombo.bienestar.exceptions.BusinessException;
import com.unicolombo.bienestar.models.Actividad;
import com.unicolombo.bienestar.models.AuditoriaActividad;
import com.unicolombo.bienestar.models.TipoAccion;
import com.unicolombo.bienestar.models.Usuario;
import com.unicolombo.bienestar.repositories.AuditoriaRepository;
import com.unicolombo.bienestar.repositories.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditoriaService {
    private final AuditoriaRepository auditoriaRepository;
    private final UsuarioRepository usuarioRepository;

    @Async
    public void registrarAccion(String emailUsuario, TipoAccion accion, String detalles) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
        registrarAccion(usuario, accion, detalles);
    }

    @Async
    public void registrarAccion(Usuario usuario, TipoAccion accion, String detalles) {
        AuditoriaActividad registro = new AuditoriaActividad();
        registro.setUsuario(usuario);
        registro.setAccion(accion);
        registro.setDetalles(detalles);
        registro.setFecha(LocalDateTime.now());
        auditoriaRepository.save(registro);
    }

    @Async
    public void registrarAccion(String emailUsuario, TipoAccion accion, String detalles, Long actividadId) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                    .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

            AuditoriaActividad registro = new AuditoriaActividad();
            registro.setUsuario(usuario);
            registro.setAccion(accion);
            registro.setDetalles(detalles);
            registro.setFecha(LocalDateTime.now());

            if (actividadId != null) {
                Actividad actividad = new Actividad();
                actividad.setId(actividadId);
                registro.setActividad(actividad);
            }

            auditoriaRepository.save(registro);
        } catch (Exception e) {
            log.error("Error registrando acción de auditoría", e);
        }
    }

    public List<AuditoriaActividad> obtenerUltimas5Auditorias() {
        return auditoriaRepository.findTop5ByOrderByFechaDesc();
    }

    @Transactional
    public void eliminarRegistrosPorActividad(Long actividadId) {
        auditoriaRepository.deleteByActividadId(actividadId);
    }
}