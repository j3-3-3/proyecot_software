package com.logistica.service;

import com.logistica.model.*;
import com.logistica.repository.EntregaRepository;
import com.logistica.repository.VehiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EntregaService {

    private final EntregaRepository entregaRepository;
    private final VehiculoRepository vehiculoRepository;

    public List<Entrega> findAll() {
        return entregaRepository.findAllByOrderByFechaPrevistaAsc();
    }

    public Entrega findById(Long id) {
        return entregaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entrega no encontrada: " + id));
    }

    public List<Entrega> findByRepartidor(Usuario repartidor) {
        return entregaRepository.findByRepartidorOrderByFechaPrevistaAsc(repartidor);
    }

    @Transactional
    public Entrega crearEntrega(Pedido pedido, LocalDate fechaPrevista) {
        Entrega entrega = new Entrega();
        entrega.setPedido(pedido);
        entrega.setEstado(EstadoEntrega.EN_PREPARACION);
        entrega.setFechaPrevista(fechaPrevista);
        entrega.setFechaActualizacion(LocalDateTime.now());
        return entregaRepository.save(entrega);
    }

    @Transactional
    public Entrega asignarRepartidor(Long entregaId, Usuario repartidor) {
        Entrega entrega = findById(entregaId);
        entrega.setRepartidor(repartidor);
        entrega.setFechaActualizacion(LocalDateTime.now());
        return entregaRepository.save(entrega);
    }

    @Transactional
    public Entrega asignarVehiculo(Long entregaId, Long vehiculoId) {
        Entrega entrega = findById(entregaId);
        Vehiculo vehiculo = vehiculoRepository.findById(vehiculoId)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado: " + vehiculoId));
        entrega.setVehiculo(vehiculo);
        entrega.setFechaActualizacion(LocalDateTime.now());
        return entregaRepository.save(entrega);
    }

    @Transactional
    public Entrega actualizarEstado(Long entregaId, EstadoEntrega nuevoEstado, String observaciones) {
        Entrega entrega = findById(entregaId);
        entrega.setEstado(nuevoEstado);
        if (observaciones != null && !observaciones.isBlank()) {
            entrega.setObservaciones(observaciones);
        }
        entrega.setFechaActualizacion(LocalDateTime.now());
        return entregaRepository.save(entrega);
    }

    @Transactional
    public Entrega modificarFecha(Long entregaId, LocalDate nuevaFecha) {
        Entrega entrega = findById(entregaId);
        entrega.setFechaPrevista(nuevaFecha);
        entrega.setFechaActualizacion(LocalDateTime.now());
        return entregaRepository.save(entrega);
    }

    public List<Vehiculo> findVehiculosDisponibles() {
        return vehiculoRepository.findByDisponibleTrue();
    }

    public List<Vehiculo> findAllVehiculos() {
        return vehiculoRepository.findAll();
    }

    @Transactional
    public Vehiculo guardarVehiculo(Vehiculo vehiculo) {
        return vehiculoRepository.save(vehiculo);
    }

    @Transactional
    public void eliminarVehiculo(Long id) {
        vehiculoRepository.deleteById(id);
    }

    public Vehiculo findVehiculoById(Long id) {
        return vehiculoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado: " + id));
    }
}
