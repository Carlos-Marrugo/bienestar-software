package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.exceptions.ActividadNoDisponibleException;
import com.unicolombo.bienestar.models.Actividad;
import com.unicolombo.bienestar.repositories.ActividadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ActividadServiceTest {

    @Mock
    private ActividadRepository actividadRepository;

    @InjectMocks
    private ActividadService actividadService;

    @Test
    public void testGetActividadById() {
        Actividad actividad = new Actividad();
        actividad.setId(1L);
        when(actividadRepository.findById(1L)).thenReturn(Optional.of(actividad));

        Actividad result = actividadService.getActividadById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    public void testGetActividadByIdNotFound() {
        when(actividadRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ActividadNoDisponibleException.class, () -> actividadService.getActividadById(1L));
    }
}