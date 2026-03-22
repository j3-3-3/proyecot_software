package com.logistica.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(length = 50)
    private String categoria;

    @Column(length = 50)
    private String ubicacion;

    @Min(0)
    @Column(nullable = false)
    private Integer stock = 0;

    @Min(0)
    @Column(nullable = false)
    private Integer stockMinimo = 5;

    @Column(length = 100)
    private String proveedor;

    @OneToMany(mappedBy = "producto")
    private List<DetallePedido> detalles;

    @OneToMany(mappedBy = "producto")
    private List<MovimientoInventario> movimientos;
}
