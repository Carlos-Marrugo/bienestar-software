package com.unicolombo.bienestar.models;

import java.time.DayOfWeek;

public enum DiaSemana {
    LUNES(DayOfWeek.MONDAY, "Lunes"),
    MARTES(DayOfWeek.TUESDAY, "Martes"),
    MIERCOLES(DayOfWeek.WEDNESDAY, "Miércoles"),
    JUEVES(DayOfWeek.THURSDAY, "Jueves"),
    VIERNES(DayOfWeek.FRIDAY, "Viernes"),
    SABADO(DayOfWeek.SATURDAY, "Sábado"),
    DOMINGO(DayOfWeek.SUNDAY, "Domingo");

    private final DayOfWeek dayOfWeek;
    private final String nombre;

    DiaSemana(DayOfWeek dayOfWeek, String nombre) {
        this.dayOfWeek = dayOfWeek;
        this.nombre = nombre;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public String getNombre() {
        return nombre;
    }

    public static DiaSemana fromDayOfWeek(DayOfWeek dayOfWeek) {
        for (DiaSemana dia : DiaSemana.values()) {
            if (dia.dayOfWeek == dayOfWeek) {
                return dia;
            }
        }
        throw new IllegalArgumentException("Día no válido: " + dayOfWeek);
    }
}