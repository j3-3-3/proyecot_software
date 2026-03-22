package com.logistica.service;

import com.logistica.model.MovimientoInventario;
import com.logistica.model.Producto;
import com.logistica.model.Usuario;
import com.logistica.repository.MovimientoInventarioRepository;
import com.logistica.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    public List<Producto> findAllProductos() {
        return productoRepository.findAll();
    }

    public Producto findProductoById(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + id));
    }

    public List<Producto> buscarProductos(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return productoRepository.findAll();
        }
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    @Transactional
    public Producto guardarProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    @Transactional
    public void eliminarProducto(Long id) {
        productoRepository.deleteById(id);
    }

    @Transactional
    public MovimientoInventario registrarEntrada(Long productoId, Integer cantidad, String lote,
                                                  String motivo, Usuario usuario) {
        Producto producto = findProductoById(productoId);
        producto.setStock(producto.getStock() + cantidad);
        productoRepository.save(producto);

        MovimientoInventario mov = new MovimientoInventario();
        mov.setProducto(producto);
        mov.setCantidad(cantidad);
        mov.setTipo("ENTRADA");
        mov.setLote(lote);
        mov.setMotivo(motivo);
        mov.setUsuario(usuario);
        return movimientoRepository.save(mov);
    }

    @Transactional
    public MovimientoInventario ajustarStock(Long productoId, Integer nuevaCantidad,
                                              String motivo, Usuario usuario) {
        Producto producto = findProductoById(productoId);
        int diferencia = nuevaCantidad - producto.getStock();
        producto.setStock(nuevaCantidad);
        productoRepository.save(producto);

        MovimientoInventario mov = new MovimientoInventario();
        mov.setProducto(producto);
        mov.setCantidad(Math.abs(diferencia));
        mov.setTipo("AJUSTE");
        mov.setMotivo(motivo);
        mov.setUsuario(usuario);
        return movimientoRepository.save(mov);
    }

    public List<MovimientoInventario> findAllMovimientos() {
        return movimientoRepository.findAllByOrderByFechaDesc();
    }

    public List<Producto> findProductosBajoStock() {
        return productoRepository.findAll().stream()
                .filter(p -> p.getStock() <= p.getStockMinimo())
                .toList();
    }
}
