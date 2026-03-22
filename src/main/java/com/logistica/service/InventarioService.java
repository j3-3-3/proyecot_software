package com.logistica.service;

import com.logistica.entity.Inventario;
import com.logistica.entity.Producto;
import com.logistica.repository.InventarioRepository;
import com.logistica.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InventarioService {

    private final InventarioRepository inventarioRepository;
    private final ProductoRepository productoRepository;

    public InventarioService(InventarioRepository inventarioRepository, ProductoRepository productoRepository) {
        this.inventarioRepository = inventarioRepository;
        this.productoRepository = productoRepository;
    }

    public List<Inventario> findAll() {
        return inventarioRepository.findAll();
    }

    public Optional<Inventario> findById(Long id) {
        return inventarioRepository.findById(id);
    }

    public Optional<Inventario> findByProductoId(Long productoId) {
        return inventarioRepository.findByProductoId(productoId);
    }

    public Inventario registrarEntrada(Inventario inventario) {
        Optional<Inventario> existing = inventarioRepository.findByProducto(inventario.getProducto());
        if (existing.isPresent()) {
            Inventario inv = existing.get();
            inv.setCantidad(inv.getCantidad() + inventario.getCantidad());
            inv.setUbicacion(inventario.getUbicacion());
            if (inventario.getNumeroLote() != null) inv.setNumeroLote(inventario.getNumeroLote());
            if (inventario.getMotivoUltimoCambio() != null) inv.setMotivoUltimoCambio(inventario.getMotivoUltimoCambio());
            return inventarioRepository.save(inv);
        }
        return inventarioRepository.save(inventario);
    }

    public Inventario modificar(Inventario inventario) {
        return inventarioRepository.save(inventario);
    }

    public boolean reservarStock(Long productoId, int cantidad) {
        Optional<Inventario> inv = inventarioRepository.findByProductoId(productoId);
        if (inv.isPresent()) {
            Inventario i = inv.get();
            if (i.getStockDisponible() >= cantidad) {
                i.setCantidadReservada(i.getCantidadReservada() + cantidad);
                inventarioRepository.save(i);
                return true;
            }
        }
        return false;
    }

    public void liberarStock(Long productoId, int cantidad) {
        inventarioRepository.findByProductoId(productoId).ifPresent(i -> {
            int nuevaReserva = Math.max(0, i.getCantidadReservada() - cantidad);
            i.setCantidadReservada(nuevaReserva);
            inventarioRepository.save(i);
        });
    }

    public void descontarStock(Long productoId, int cantidad) {
        inventarioRepository.findByProductoId(productoId).ifPresent(i -> {
            i.setCantidad(Math.max(0, i.getCantidad() - cantidad));
            i.setCantidadReservada(Math.max(0, i.getCantidadReservada() - cantidad));
            inventarioRepository.save(i);
        });
    }

    public List<Inventario> findProductosBajoStock() {
        return inventarioRepository.findProductosBajoStock();
    }

    public List<Producto> findProductosActivos() {
        return productoRepository.findByActivoTrue();
    }
}
