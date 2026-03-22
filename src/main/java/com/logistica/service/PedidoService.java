package com.logistica.service;

import com.logistica.model.*;
import com.logistica.repository.DetallePedidoRepository;
import com.logistica.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final InventarioService inventarioService;

    public List<Pedido> findAll() {
        return pedidoRepository.findAllByOrderByFechaSolicitudDesc();
    }

    public List<Pedido> findByCliente(Usuario cliente) {
        return pedidoRepository.findByClienteOrderByFechaSolicitudDesc(cliente);
    }

    public Pedido findById(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + id));
    }

    @Transactional
    public Pedido crearPedido(Usuario cliente, String direccion, String observaciones,
                               Map<Long, Integer> productoCantidades) {
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setDireccionEntrega(direccion);
        pedido.setObservaciones(observaciones);
        pedido.setEstado(EstadoPedido.EN_PREPARACION);
        pedido.setFechaSolicitud(LocalDateTime.now());
        pedido = pedidoRepository.save(pedido);

        for (Map.Entry<Long, Integer> entry : productoCantidades.entrySet()) {
            Producto producto = inventarioService.findProductoById(entry.getKey());
            int cantidad = entry.getValue();
            if (producto.getStock() < cantidad) {
                throw new IllegalStateException("Stock insuficiente para: " + producto.getNombre());
            }
            producto.setStock(producto.getStock() - cantidad);
            inventarioService.guardarProducto(producto);

            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detallePedidoRepository.save(detalle);
        }

        return pedido;
    }

    @Transactional
    public void cancelarPedido(Long pedidoId) {
        Pedido pedido = findById(pedidoId);
        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new IllegalStateException("El pedido ya está cancelado");
        }
        // Devolver stock
        for (DetallePedido detalle : pedido.getDetalles()) {
            Producto p = detalle.getProducto();
            p.setStock(p.getStock() + detalle.getCantidad());
            inventarioService.guardarProducto(p);
        }
        pedido.setEstado(EstadoPedido.CANCELADO);
        pedidoRepository.save(pedido);
    }

    @Transactional
    public void actualizarEstado(Long pedidoId, EstadoPedido nuevoEstado) {
        Pedido pedido = findById(pedidoId);
        pedido.setEstado(nuevoEstado);
        pedidoRepository.save(pedido);
    }
}
