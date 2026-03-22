package com.logistica.repository;

import com.logistica.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    List<Producto> findByCategoriaIgnoreCase(String categoria);
    List<Producto> findByUbicacionIgnoreCase(String ubicacion);
    List<Producto> findByStockLessThan(Integer stock);
}
