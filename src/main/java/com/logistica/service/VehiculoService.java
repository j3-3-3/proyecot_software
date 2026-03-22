package com.logistica.service;

import com.logistica.entity.Vehiculo;
import com.logistica.repository.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;

    public VehiculoService(VehiculoRepository vehiculoRepository) {
        this.vehiculoRepository = vehiculoRepository;
    }

    public List<Vehiculo> findAll() {
        return vehiculoRepository.findAll();
    }

    public List<Vehiculo> findDisponibles() {
        return vehiculoRepository.findByDisponibleTrue();
    }

    public Optional<Vehiculo> findById(Long id) {
        return vehiculoRepository.findById(id);
    }

    public Vehiculo save(Vehiculo vehiculo) {
        return vehiculoRepository.save(vehiculo);
    }

    public void delete(Long id) {
        vehiculoRepository.deleteById(id);
    }

    public boolean existsByMatricula(String matricula) {
        return vehiculoRepository.existsByMatricula(matricula);
    }
}
