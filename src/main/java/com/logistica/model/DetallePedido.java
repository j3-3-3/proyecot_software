package com.logistica.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "detalles_pedido")
@Getter
@Setter
@NoArgsConstructor
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Min(1)
    @Column(nullable = false)
    private Integer cantidad;
}
