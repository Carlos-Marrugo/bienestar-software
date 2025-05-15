package com.unicolombo.bienestar.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RegistroHorasDto {

    @NotNull(message = "El ID del estudiante es obligatorio")
    private Long estudianteId;

    @NotNull(message = "Las horas son obligatorias")
    @Min(value = 1, message = "Debe registrar al menos 1 hora")
    private Integer horas;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    // Getters y setters
    public Long getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(Long estudianteId) {
        this.estudianteId = estudianteId;
    }

    public Integer getHoras() {
        return horas;
    }

    public void setHoras(Integer horas) {
        this.horas = horas;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
