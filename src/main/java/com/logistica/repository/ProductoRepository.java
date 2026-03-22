package com.logistica.repository;

import com.logistica.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByActivoTrue();
    List<Producto> findByCategoriaAndActivoTrue(String categoria);

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND " +
           "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.categoria) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Producto> searchActivos(@Param("query") String query);

    @Query("SELECT DISTINCT p.categoria FROM Producto p WHERE p.activo = true ORDER BY p.categoria")
    List<String> findCategoriasActivas();
}
