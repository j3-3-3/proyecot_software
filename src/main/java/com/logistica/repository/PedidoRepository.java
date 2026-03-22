package com.logistica.repository;

import com.logistica.model.Pedido;
import com.logistica.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByClienteOrderByFechaPedidoDesc(Usuario cliente);
    List<Pedido> findAllByOrderByFechaPedidoDesc();
    List<Pedido> findByEstado(Pedido.EstadoPedido estado);
}
