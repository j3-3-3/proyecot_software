package com.logistica.repository;

import com.logistica.entity.EntregaProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntregaProductoRepository extends JpaRepository<EntregaProducto, Long> {
    List<EntregaProducto> findByEntregaId(Long entregaId);
}
