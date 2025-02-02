package com.unicolombo.bienestar.models;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class Estudiante extends Usuario {
    private int horasAcumuladas = 0;
}