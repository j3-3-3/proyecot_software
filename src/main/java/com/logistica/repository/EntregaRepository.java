package com.logistica.repository;

import com.logistica.entity.Entrega;
import com.logistica.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntregaRepository extends JpaRepository<Entrega, Long> {
    List<Entrega> findByCliente(Usuario cliente);
    List<Entrega> findByRepartidor(Usuario repartidor);
    List<Entrega> findByEstado(Entrega.Estado estado);
    List<Entrega> findByClienteOrderByFechaPedidoDesc(Usuario cliente);
    List<Entrega> findByRepartidorOrderByFechaPedidoDesc(Usuario repartidor);
    Optional<Entrega> findByNumeroPedido(String numeroPedido);
}
