package com.logistica.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @NotBlank(message = "La categoría es obligatoria")
    @Column(nullable = false)
    private String categoria;

    @NotBlank(message = "El proveedor es obligatorio")
    @Column(nullable = false)
    private String proveedor;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser positivo")
    @Column(nullable = false)
    private Double precio;

    @Column(name = "stock_minimo")
    private Integer stockMinimo = 0;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @OneToOne(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Inventario inventario;

    @OneToMany(mappedBy = "producto", fetch = FetchType.LAZY)
    private List<EntregaProducto> entregaProductos;
}
