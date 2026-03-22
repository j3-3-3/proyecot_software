package com.logistica.service;

import com.logistica.model.Vehiculo;
import com.logistica.repository.VehiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;

    @Transactional(readOnly = true)
    public List<Vehiculo> findAll() {
        return vehiculoRepository.findByActivoTrue();
    }

    @Transactional(readOnly = true)
    public List<Vehiculo> findDisponibles() {
        return vehiculoRepository.findByDisponibleTrueAndActivoTrue();
    }

    @Transactional(readOnly = true)
    public Optional<Vehiculo> findById(Long id) {
        return vehiculoRepository.findById(id);
    }

    @Transactional
    public Vehiculo save(Vehiculo vehiculo) {
        return vehiculoRepository.save(vehiculo);
    }

    @Transactional
    public void softDelete(Long id) {
        vehiculoRepository.findById(id).ifPresent(v -> {
            v.setActivo(false);
            vehiculoRepository.save(v);
        });
    }
}
