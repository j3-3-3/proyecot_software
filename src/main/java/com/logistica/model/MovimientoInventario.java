package com.logistica.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_inventario")
@Getter
@Setter
@NoArgsConstructor
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false, length = 20)
    private String tipo; // ENTRADA, SALIDA, AJUSTE

    @Column(length = 255)
    private String motivo;

    @Column(length = 100)
    private String lote;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();
}
