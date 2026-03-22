package com.logistica.repository;

import com.logistica.model.DetallePedido;
import com.logistica.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {
    List<DetallePedido> findByPedido(Pedido pedido);
}
