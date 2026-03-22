package com.logistica.service;

import com.logistica.dto.ItemPedidoForm;
import com.logistica.dto.PedidoForm;
import com.logistica.model.*;
import com.logistica.repository.PedidoRepository;
import com.logistica.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public List<Pedido> findAll() {
        return pedidoRepository.findAllByOrderByFechaPedidoDesc();
    }

    @Transactional(readOnly = true)
    public List<Pedido> findByCliente(Usuario cliente) {
        return pedidoRepository.findByClienteOrderByFechaPedidoDesc(cliente);
    }

    @Transactional(readOnly = true)
    public Optional<Pedido> findById(Long id) {
        return pedidoRepository.findById(id);
    }

    @Transactional
    public Pedido crearPedido(PedidoForm form, Usuario cliente) {
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setDireccionEntrega(form.getDireccionEntrega());
        pedido.setObservaciones(form.getObservaciones());
        pedido.setEstado(Pedido.EstadoPedido.PENDIENTE);

        for (ItemPedidoForm itemForm : form.getItems()) {
            if (itemForm.getProductoId() == null || itemForm.getCantidad() == null || itemForm.getCantidad() <= 0) {
                continue;
            }
            Optional<Producto> productoOpt = productoRepository.findById(itemForm.getProductoId());
            if (productoOpt.isEmpty()) continue;
            Producto producto = productoOpt.get();
            if (!producto.isActivo() || producto.getCantidadStock() < itemForm.getCantidad()) {
                throw new IllegalArgumentException("Stock insuficiente para el producto: " + producto.getNombre());
            }
            producto.setCantidadStock(producto.getCantidadStock() - itemForm.getCantidad());
            productoRepository.save(producto);

            ItemPedido item = new ItemPedido();
            item.setPedido(pedido);
            item.setProducto(producto);
            item.setCantidad(itemForm.getCantidad());
            item.setPrecioUnitario(producto.getPrecioUnitario());
            pedido.getItems().add(item);
        }

        if (pedido.getItems().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe tener al menos un producto.");
        }

        return pedidoRepository.save(pedido);
    }

    @Transactional
    public void cancelar(Long id) {
        pedidoRepository.findById(id).ifPresent(p -> {
            if (p.getEstado() == Pedido.EstadoPedido.PENDIENTE ||
                p.getEstado() == Pedido.EstadoPedido.EN_PREPARACION) {
                // Restore stock
                for (ItemPedido item : p.getItems()) {
                    Producto prod = item.getProducto();
                    prod.setCantidadStock(prod.getCantidadStock() + item.getCantidad());
                    productoRepository.save(prod);
                }
                p.setEstado(Pedido.EstadoPedido.CANCELADO);
                pedidoRepository.save(p);
            }
        });
    }

    @Transactional
    public void aprobar(Long id) {
        pedidoRepository.findById(id).ifPresent(p -> {
            if (p.getEstado() == Pedido.EstadoPedido.PENDIENTE) {
                p.setEstado(Pedido.EstadoPedido.EN_PREPARACION);
                pedidoRepository.save(p);
            }
        });
    }

    @Transactional
    public Pedido save(Pedido pedido) {
        return pedidoRepository.save(pedido);
    }

    public BigDecimal calcularTotal(Pedido pedido) {
        return pedido.getItems().stream()
                .map(ItemPedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
