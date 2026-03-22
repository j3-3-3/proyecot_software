package com.logistica.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id")
    private Usuario cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado = EstadoPedido.EN_PREPARACION;

    @Column(nullable = false)
    private LocalDateTime fechaSolicitud = LocalDateTime.now();

    @Column(length = 255)
    private String direccionEntrega;

    @Column(length = 500)
    private String observaciones;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    @OneToOne(mappedBy = "pedido")
    private Entrega entrega;
}
