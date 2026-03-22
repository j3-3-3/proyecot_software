package com.logistica.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "entregas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_pedido", unique = true)
    private String numeroPedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repartidor_id")
    private Usuario repartidor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;

    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado = Estado.EN_PREPARACION;

    @NotBlank(message = "La dirección de entrega es obligatoria")
    @Column(name = "direccion_entrega", nullable = false)
    private String direccionEntrega;

    @Column(name = "fecha_pedido")
    private LocalDateTime fechaPedido;

    @Column(name = "fecha_entrega_prevista")
    private LocalDate fechaEntregaPrevista;

    @Column(name = "fecha_entrega_real")
    private LocalDateTime fechaEntregaReal;

    @Column(name = "notas", length = 500)
    private String notas;

    @OneToMany(mappedBy = "entrega", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EntregaProducto> productos;

    @PrePersist
    public void prePersist() {
        this.fechaPedido = LocalDateTime.now();
        this.numeroPedido = "PED-" + System.currentTimeMillis();
    }

    public enum Estado {
        EN_PREPARACION("En Preparación"),
        PENDIENTE("Pendiente"),
        EN_REPARTO("En Reparto"),
        INCIDENCIA("Incidencia"),
        ENTREGADO("Entregado"),
        CANCELADO("Cancelado");

        private final String label;

        Estado(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}
