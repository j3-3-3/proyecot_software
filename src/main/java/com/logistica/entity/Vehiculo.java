package com.logistica.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "vehiculos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La matrícula es obligatoria")
    @Column(nullable = false, unique = true)
    private String matricula;

    @NotBlank(message = "El modelo es obligatorio")
    @Column(nullable = false)
    private String modelo;

    @NotBlank(message = "La marca es obligatoria")
    @Column(nullable = false)
    private String marca;

    @NotNull(message = "El año es obligatorio")
    @Column(nullable = false)
    private Integer anio;

    @Column(nullable = false)
    private boolean disponible = true;

    @Column(name = "fecha_ultimo_mantenimiento")
    private LocalDate fechaUltimoMantenimiento;

    @Column(name = "fecha_proximo_mantenimiento")
    private LocalDate fechaProximoMantenimiento;

    @Column(name = "notas_mantenimiento", length = 500)
    private String notasMantenimiento;

    public boolean isMantenimientoPorVencer() {
        if (fechaProximoMantenimiento == null) return false;
        return fechaProximoMantenimiento.isBefore(LocalDate.now().plusDays(15));
    }
}
