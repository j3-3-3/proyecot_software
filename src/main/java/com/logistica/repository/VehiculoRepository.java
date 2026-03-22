package com.logistica.repository;

import com.logistica.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
    List<Vehiculo> findByActivoTrue();
    List<Vehiculo> findByDisponibleTrueAndActivoTrue();
    Optional<Vehiculo> findByMatricula(String matricula);
}
