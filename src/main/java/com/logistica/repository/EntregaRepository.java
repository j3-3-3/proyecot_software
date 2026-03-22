package com.logistica.repository;

import com.logistica.model.Entrega;
import com.logistica.model.EstadoEntrega;
import com.logistica.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EntregaRepository extends JpaRepository<Entrega, Long> {
    List<Entrega> findByRepartidorOrderByFechaPrevistaAsc(Usuario repartidor);
    List<Entrega> findByEstado(EstadoEntrega estado);
    List<Entrega> findAllByOrderByFechaPrevistaAsc();
    List<Entrega> findByRepartidorAndEstadoNot(Usuario repartidor, EstadoEntrega estado);
}
