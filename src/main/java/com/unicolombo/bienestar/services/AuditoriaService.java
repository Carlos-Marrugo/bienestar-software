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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;

@Slf4j
@Service
public class AuditoriaService {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Async
    public void registrarAccion(String emailUsuario, TipoAccion accion, String detalles,
                                Long actividadId) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                    .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

            AuditoriaActividad registro = new AuditoriaActividad();
            registro.setUsuario(usuario);
            registro.setAccion(accion);
            registro.setFecha(LocalDateTime.now());
            registro.setDetalles(detalles);

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

    @Async
    public void registrarAccion(String emailUsuario, TipoAccion accion, String detalles) {
        registrarAccion(emailUsuario, accion, detalles, null);
    }

    @Async
    public void registrarAccion(Usuario usuario, String accion, String detalles) {
        AuditoriaActividad registro = new AuditoriaActividad();
        registro.setUsuario(usuario);
        registro.setAccion(TipoAccion.valueOf(accion));
        registro.setFecha(LocalDateTime.now());
        registro.setDetalles(detalles);
        auditoriaRepository.save(registro);
    }
}