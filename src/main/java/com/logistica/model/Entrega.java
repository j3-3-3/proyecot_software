package com.logistica.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "entregas")
@Getter
@Setter
@NoArgsConstructor
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "repartidor_id")
    private Usuario repartidor;

    @ManyToOne
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEntrega estado = EstadoEntrega.EN_PREPARACION;

    @Column
    private LocalDate fechaPrevista;

    @Column
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    @Column(length = 500)
    private String observaciones;
}
