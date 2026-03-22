package com.logistica.repository;

import com.logistica.model.Entrega;
import com.logistica.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntregaRepository extends JpaRepository<Entrega, Long> {
    List<Entrega> findByRepartidorOrderByFechaEntregaPrevistaAsc(Usuario repartidor);
    List<Entrega> findAllByOrderByFechaEntregaPrevistaAsc();
    Optional<Entrega> findByPedidoId(Long pedidoId);
}
