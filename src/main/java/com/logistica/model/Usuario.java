package com.logistica.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuario", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Usuario {

    public enum Rol {
        ADMIN, OPERARIO, REPARTIDOR, CLIENTE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellidos;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }
}
