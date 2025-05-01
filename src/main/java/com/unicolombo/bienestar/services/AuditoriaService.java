package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.dto.ActividadCreateDto;
import com.unicolombo.bienestar.dto.AuditoriaCreateDto;
import com.unicolombo.bienestar.models.Actividad;
import com.unicolombo.bienestar.models.AuditoriaActividad;
import com.unicolombo.bienestar.models.TipoAccion;
import com.unicolombo.bienestar.models.Usuario;
import com.unicolombo.bienestar.repositories.AuditoriaRepository;
import com.unicolombo.bienestar.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;

@Service
public class AuditoriaService {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Async
    public void registrarAccion(Actividad actividad, Usuario usuario, String accion, String detalles) {

        AuditoriaActividad registro = new AuditoriaActividad();
        registro.setActividad(actividad);
        registro.setUsuario(usuario);
        registro.setAccion(TipoAccion.valueOf(accion));
        registro.setFecha(LocalDateTime.now());
        registro.setDetalles(detalles);

        auditoriaRepository.save(registro);
    }
}