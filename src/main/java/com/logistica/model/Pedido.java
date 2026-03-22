package com.logistica.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Pedido {

    public enum EstadoPedido {
        EN_PREPARACION, PENDIENTE, EN_REPARTO, ENTREGADO, CANCELADO;

        public String getLabel() {
            return switch (this) {
                case EN_PREPARACION -> "En Preparación";
                case PENDIENTE -> "Pendiente";
                case EN_REPARTO -> "En Reparto";
                case ENTREGADO -> "Entregado";
                case CANCELADO -> "Cancelado";
            };
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @Column(name = "fecha_pedido", nullable = false)
    @Builder.Default
    private LocalDateTime fechaPedido = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @NotBlank(message = "La dirección de entrega es obligatoria")
    @Column(name = "direccion_entrega", nullable = false)
    private String direccionEntrega;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ItemPedido> items = new ArrayList<>();
}
