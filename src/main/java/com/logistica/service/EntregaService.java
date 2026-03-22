package com.logistica.service;

import com.logistica.dto.ActualizarEstadoForm;
import com.logistica.dto.AsignarEntregaForm;
import com.logistica.model.*;
import com.logistica.repository.EntregaRepository;
import com.logistica.repository.PedidoRepository;
import com.logistica.repository.UsuarioRepository;
import com.logistica.repository.VehiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EntregaService {

    private final EntregaRepository entregaRepository;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final VehiculoRepository vehiculoRepository;

    @Transactional(readOnly = true)
    public List<Entrega> findAll() {
        return entregaRepository.findAllByOrderByFechaEntregaPrevistaAsc();
    }

    @Transactional(readOnly = true)
    public List<Entrega> findByRepartidor(Usuario repartidor) {
        return entregaRepository.findByRepartidorOrderByFechaEntregaPrevistaAsc(repartidor);
    }

    @Transactional(readOnly = true)
    public Optional<Entrega> findById(Long id) {
        return entregaRepository.findById(id);
    }

    @Transactional
    public Entrega crearEntregaParaPedido(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));
        Entrega entrega = new Entrega();
        entrega.setPedido(pedido);
        entrega.setEstado(Entrega.EstadoEntrega.PENDIENTE);
        pedido.setEstado(Pedido.EstadoPedido.EN_PREPARACION);
        pedidoRepository.save(pedido);
        return entregaRepository.save(entrega);
    }

    @Transactional
    public void asignar(Long id, AsignarEntregaForm form) {
        Entrega entrega = entregaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entrega no encontrada"));

        if (form.getRepartidorId() != null) {
            usuarioRepository.findById(form.getRepartidorId())
                    .ifPresent(entrega::setRepartidor);
        }
        if (form.getVehiculoId() != null) {
            vehiculoRepository.findById(form.getVehiculoId()).ifPresent(v -> {
                // Free previous vehicle if different
                if (entrega.getVehiculo() != null && !entrega.getVehiculo().getId().equals(v.getId())) {
                    entrega.getVehiculo().setDisponible(true);
                    vehiculoRepository.save(entrega.getVehiculo());
                }
                v.setDisponible(false);
                vehiculoRepository.save(v);
                entrega.setVehiculo(v);
            });
        }
        entrega.setFechaEntregaPrevista(form.getFechaEntregaPrevista());
        entrega.setNotas(form.getNotas());
        entrega.setEstado(Entrega.EstadoEntrega.PENDIENTE);
        entregaRepository.save(entrega);
    }

    @Transactional
    public void actualizarEstado(Long id, ActualizarEstadoForm form) {
        Entrega entrega = entregaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entrega no encontrada"));
        entrega.setEstado(form.getEstado());
        if (form.getNotas() != null && !form.getNotas().isBlank()) {
            entrega.setNotas(form.getNotas());
        }
        if (form.getEstado() == Entrega.EstadoEntrega.ENTREGADO) {
            entrega.setFechaEntregaReal(LocalDateTime.now());
            entrega.getPedido().setEstado(Pedido.EstadoPedido.ENTREGADO);
            if (entrega.getVehiculo() != null) {
                entrega.getVehiculo().setDisponible(true);
                vehiculoRepository.save(entrega.getVehiculo());
            }
        } else if (form.getEstado() == Entrega.EstadoEntrega.EN_REPARTO) {
            entrega.getPedido().setEstado(Pedido.EstadoPedido.EN_REPARTO);
        } else if (form.getEstado() == Entrega.EstadoEntrega.CANCELADO) {
            entrega.getPedido().setEstado(Pedido.EstadoPedido.CANCELADO);
            if (entrega.getVehiculo() != null) {
                entrega.getVehiculo().setDisponible(true);
                vehiculoRepository.save(entrega.getVehiculo());
            }
        }
        pedidoRepository.save(entrega.getPedido());
        entregaRepository.save(entrega);
    }

    @Transactional
    public void cancelar(Long id) {
        entregaRepository.findById(id).ifPresent(e -> {
            e.setEstado(Entrega.EstadoEntrega.CANCELADO);
            e.getPedido().setEstado(Pedido.EstadoPedido.CANCELADO);
            if (e.getVehiculo() != null) {
                e.getVehiculo().setDisponible(true);
                vehiculoRepository.save(e.getVehiculo());
            }
            pedidoRepository.save(e.getPedido());
            entregaRepository.save(e);
        });
    }
}
