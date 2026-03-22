package com.logistica.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "entrega")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Entrega {

    public enum EstadoEntrega {
        PENDIENTE, EN_REPARTO, INCIDENCIA, ENTREGADO, CANCELADO;

        public String getLabel() {
            return switch (this) {
                case PENDIENTE -> "Pendiente";
                case EN_REPARTO -> "En Reparto";
                case INCIDENCIA -> "Incidencia";
                case ENTREGADO -> "Entregado";
                case CANCELADO -> "Cancelado";
            };
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repartidor_id")
    private Usuario repartidor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;

    @Column(name = "fecha_entrega_prevista")
    private LocalDate fechaEntregaPrevista;

    @Column(name = "fecha_entrega_real")
    private LocalDateTime fechaEntregaReal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoEntrega estado = EstadoEntrega.PENDIENTE;

    @Column(columnDefinition = "TEXT")
    private String notas;
}
