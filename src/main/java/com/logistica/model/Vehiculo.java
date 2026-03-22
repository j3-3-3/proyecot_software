package com.logistica.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "vehiculo", uniqueConstraints = @UniqueConstraint(columnNames = "matricula"))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La matrícula es obligatoria")
    @Column(nullable = false, unique = true)
    private String matricula;

    @NotBlank(message = "La marca es obligatoria")
    @Column(nullable = false)
    private String marca;

    @NotBlank(message = "El modelo es obligatorio")
    @Column(nullable = false)
    private String modelo;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    @Builder.Default
    private boolean disponible = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    public String getDescripcion() {
        return marca + " " + modelo + " (" + matricula + ")";
    }
}
