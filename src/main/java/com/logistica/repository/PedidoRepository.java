package com.logistica.repository;

import com.logistica.model.EstadoPedido;
import com.logistica.model.Pedido;
import com.logistica.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByClienteOrderByFechaSolicitudDesc(Usuario cliente);
    List<Pedido> findByEstado(EstadoPedido estado);
    List<Pedido> findAllByOrderByFechaSolicitudDesc();
}
