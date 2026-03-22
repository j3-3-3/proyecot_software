package com.logistica.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "producto")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @NotBlank(message = "La categoría es obligatoria")
    @Column(nullable = false)
    private String categoria;

    @NotNull(message = "La cantidad de stock es obligatoria")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(name = "cantidad_stock", nullable = false)
    private Integer cantidadStock;

    @Column(nullable = false)
    private String ubicacion;

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor que 0")
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;
}
