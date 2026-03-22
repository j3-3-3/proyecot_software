package com.logistica.repository;

import com.logistica.model.Rol;
import com.logistica.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    List<Usuario> findByRol(Rol rol);
    List<Usuario> findByActivoTrue();
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
