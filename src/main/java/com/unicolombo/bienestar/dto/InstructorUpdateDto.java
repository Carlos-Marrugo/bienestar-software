package com.unicolombo.bienestar.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class InstructorUpdateDto {
    @NotBlank(message = "Especialidad es obligatoria")
    private String especialidad;

    @NotNull(message = "Fecha de contratación es obligatoria")
    @PastOrPresent(message = "Fecha de contratación no puede ser futura")
    private LocalDate fechaContratacion;
}
