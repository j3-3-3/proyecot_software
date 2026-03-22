package com.logistica.repository;

import com.logistica.entity.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
    List<Vehiculo> findByDisponibleTrue();
    Optional<Vehiculo> findByMatricula(String matricula);
    boolean existsByMatricula(String matricula);
}
