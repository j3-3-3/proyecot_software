package com.logistica.service;

import com.logistica.entity.*;
import com.logistica.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EntregaService {

    private final EntregaRepository entregaRepository;
    private final EntregaProductoRepository entregaProductoRepository;
    private final InventarioService inventarioService;
    private final VehiculoRepository vehiculoRepository;

    public EntregaService(EntregaRepository entregaRepository,
                          EntregaProductoRepository entregaProductoRepository,
                          InventarioService inventarioService,
                          VehiculoRepository vehiculoRepository) {
        this.entregaRepository = entregaRepository;
        this.entregaProductoRepository = entregaProductoRepository;
        this.inventarioService = inventarioService;
        this.vehiculoRepository = vehiculoRepository;
    }

    public List<Entrega> findAll() {
        return entregaRepository.findAll();
    }

    public Optional<Entrega> findById(Long id) {
        return entregaRepository.findById(id);
    }

    public List<Entrega> findByCliente(Usuario cliente) {
        return entregaRepository.findByClienteOrderByFechaPedidoDesc(cliente);
    }

    public List<Entrega> findByRepartidor(Usuario repartidor) {
        return entregaRepository.findByRepartidorOrderByFechaPedidoDesc(repartidor);
    }

    public List<Entrega> findByEstado(Entrega.Estado estado) {
        return entregaRepository.findByEstado(estado);
    }

    public Entrega crearPedido(Entrega entrega, List<EntregaProducto> productos) {
        // Reserve stock for each product
        for (EntregaProducto ep : productos) {
            boolean reservado = inventarioService.reservarStock(ep.getProducto().getId(), ep.getCantidad());
            if (!reservado) {
                throw new IllegalStateException("Stock insuficiente para el producto: " + ep.getProducto().getNombre());
            }
        }
        entrega.setEstado(Entrega.Estado.EN_PREPARACION);
        Entrega saved = entregaRepository.save(entrega);

        for (EntregaProducto ep : productos) {
            ep.setEntrega(saved);
            ep.setPrecioUnitario(ep.getProducto().getPrecio());
            entregaProductoRepository.save(ep);
        }

        return saved;
    }

    public Entrega actualizarEstado(Long id, Entrega.Estado nuevoEstado, String username) {
        Entrega entrega = entregaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entrega no encontrada: " + id));

        Entrega.Estado estadoActual = entrega.getEstado();

        // Validate state transitions
        if (!isTransicionValida(estadoActual, nuevoEstado)) {
            throw new IllegalStateException("Transición de estado inválida: " + estadoActual + " -> " + nuevoEstado);
        }

        entrega.setEstado(nuevoEstado);

        if (nuevoEstado == Entrega.Estado.ENTREGADO) {
            entrega.setFechaEntregaReal(LocalDateTime.now());
            // Discount reserved stock
            if (entrega.getProductos() != null) {
                entrega.getProductos().forEach(ep ->
                    inventarioService.descontarStock(ep.getProducto().getId(), ep.getCantidad())
                );
            }
        }

        return entregaRepository.save(entrega);
    }

    public Entrega cancelarEntrega(Long id) {
        Entrega entrega = entregaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entrega no encontrada: " + id));

        Entrega.Estado estado = entrega.getEstado();
        if (estado == Entrega.Estado.ENTREGADO || estado == Entrega.Estado.CANCELADO) {
            throw new IllegalStateException("No se puede cancelar una entrega en estado: " + estado.getLabel());
        }

        // Release reserved stock
        if (entrega.getProductos() != null) {
            entrega.getProductos().forEach(ep ->
                inventarioService.liberarStock(ep.getProducto().getId(), ep.getCantidad())
            );
        }

        // Release vehicle if assigned
        if (entrega.getVehiculo() != null) {
            Vehiculo v = entrega.getVehiculo();
            v.setDisponible(true);
            vehiculoRepository.save(v);
        }

        entrega.setEstado(Entrega.Estado.CANCELADO);
        return entregaRepository.save(entrega);
    }

    public Entrega asignarRepartidor(Long entregaId, Usuario repartidor) {
        Entrega entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new IllegalArgumentException("Entrega no encontrada: " + entregaId));
        entrega.setRepartidor(repartidor);
        entrega.setEstado(Entrega.Estado.PENDIENTE);
        return entregaRepository.save(entrega);
    }

    public Entrega asignarVehiculo(Long entregaId, Vehiculo vehiculo) {
        Entrega entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new IllegalArgumentException("Entrega no encontrada: " + entregaId));
        // Free previous vehicle if any
        if (entrega.getVehiculo() != null && !entrega.getVehiculo().getId().equals(vehiculo.getId())) {
            entrega.getVehiculo().setDisponible(true);
            vehiculoRepository.save(entrega.getVehiculo());
        }
        vehiculo.setDisponible(false);
        vehiculoRepository.save(vehiculo);
        entrega.setVehiculo(vehiculo);
        return entregaRepository.save(entrega);
    }

    public Entrega save(Entrega entrega) {
        return entregaRepository.save(entrega);
    }

    private boolean isTransicionValida(Entrega.Estado actual, Entrega.Estado nuevo) {
        return switch (actual) {
            case EN_PREPARACION -> nuevo == Entrega.Estado.PENDIENTE || nuevo == Entrega.Estado.CANCELADO;
            case PENDIENTE -> nuevo == Entrega.Estado.EN_REPARTO || nuevo == Entrega.Estado.CANCELADO;
            case EN_REPARTO -> nuevo == Entrega.Estado.ENTREGADO || nuevo == Entrega.Estado.INCIDENCIA;
            case INCIDENCIA -> nuevo == Entrega.Estado.EN_REPARTO || nuevo == Entrega.Estado.CANCELADO;
            case ENTREGADO, CANCELADO -> false;
        };
    }
}
