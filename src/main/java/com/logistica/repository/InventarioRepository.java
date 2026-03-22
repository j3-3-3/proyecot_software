package com.logistica.repository;

import com.logistica.entity.Inventario;
import com.logistica.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {
    Optional<Inventario> findByProducto(Producto producto);
    Optional<Inventario> findByProductoId(Long productoId);
    List<Inventario> findByUbicacionContainingIgnoreCase(String ubicacion);

    @Query("SELECT i FROM Inventario i WHERE i.cantidad <= i.producto.stockMinimo")
    List<Inventario> findProductosBajoStock();
}
