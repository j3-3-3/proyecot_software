package com.logistica.repository;

import com.logistica.model.MovimientoInventario;
import com.logistica.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    List<MovimientoInventario> findByProductoOrderByFechaDesc(Producto producto);
    List<MovimientoInventario> findAllByOrderByFechaDesc();
}
