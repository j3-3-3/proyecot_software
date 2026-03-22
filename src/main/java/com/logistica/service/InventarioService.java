package com.logistica.service;

import com.logistica.model.Producto;
import com.logistica.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public List<Producto> findAll() {
        return productoRepository.findByActivoTrue();
    }

    @Transactional(readOnly = true)
    public List<Producto> search(String query) {
        if (query == null || query.isBlank()) {
            return productoRepository.findByActivoTrue();
        }
        return productoRepository.searchActivos(query.trim());
    }

    @Transactional(readOnly = true)
    public Optional<Producto> findById(Long id) {
        return productoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<String> getCategorias() {
        return productoRepository.findCategoriasActivas();
    }

    @Transactional
    public Producto save(Producto producto) {
        return productoRepository.save(producto);
    }

    @Transactional
    public void softDelete(Long id) {
        productoRepository.findById(id).ifPresent(p -> {
            p.setActivo(false);
            productoRepository.save(p);
        });
    }

    @Transactional
    public boolean adjustStock(Long productoId, int cantidad) {
        Optional<Producto> opt = productoRepository.findById(productoId);
        if (opt.isEmpty()) return false;
        Producto p = opt.get();
        if (p.getCantidadStock() < cantidad) return false;
        p.setCantidadStock(p.getCantidadStock() - cantidad);
        productoRepository.save(p);
        return true;
    }

    @Transactional
    public void restoreStock(Long productoId, int cantidad) {
        productoRepository.findById(productoId).ifPresent(p -> {
            p.setCantidadStock(p.getCantidadStock() + cantidad);
            productoRepository.save(p);
        });
    }
}
