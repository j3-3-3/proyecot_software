package com.logistica.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehiculos")
@Getter
@Setter
@NoArgsConstructor
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String matricula;

    @Column(length = 50)
    private String marca;

    @Column(length = 50)
    private String modelo;

    @Column(length = 20)
    private String tipo; // FURGONETA, CAMION, MOTO

    @Column(nullable = false)
    private boolean disponible = true;
}
