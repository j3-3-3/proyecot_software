package com.logistica.config;

import com.logistica.model.Producto;
import com.logistica.model.Rol;
import com.logistica.model.Vehiculo;
import com.logistica.repository.ProductoRepository;
import com.logistica.repository.VehiculoRepository;
import com.logistica.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UsuarioService usuarioService;
    private final ProductoRepository productoRepository;
    private final VehiculoRepository vehiculoRepository;

    @Bean
    @Profile("!test")
    public CommandLineRunner initData() {
        return args -> {
            // Create admin user if not exists
            try {
                usuarioService.registrarUsuario("admin", "Admin1234!", "Administrador", "admin@logistica.com", Rol.ADMIN);
                log.info("Usuario admin creado");
            } catch (IllegalArgumentException e) {
                log.info("Usuario admin ya existe");
            }

            // Create operario user
            try {
                usuarioService.registrarUsuario("operario1", "Admin1234!", "Juan García", "operario1@logistica.com", Rol.OPERARIO);
                log.info("Usuario operario1 creado");
            } catch (IllegalArgumentException e) {
                log.info("Usuario operario1 ya existe");
            }

            // Create repartidor user
            try {
                usuarioService.registrarUsuario("repartidor1", "Admin1234!", "Carlos López", "repartidor1@logistica.com", Rol.REPARTIDOR);
                log.info("Usuario repartidor1 creado");
            } catch (IllegalArgumentException e) {
                log.info("Usuario repartidor1 ya existe");
            }

            // Create cliente user
            try {
                usuarioService.registrarUsuario("cliente1", "Admin1234!", "María Martínez", "cliente1@logistica.com", Rol.CLIENTE);
                log.info("Usuario cliente1 creado");
            } catch (IllegalArgumentException e) {
                log.info("Usuario cliente1 ya existe");
            }

            // Create sample products if none exist
            if (productoRepository.count() == 0) {
                Producto p1 = new Producto();
                p1.setNombre("Caja de cartón grande");
                p1.setDescripcion("Caja de embalaje 60x40x40cm");
                p1.setCategoria("Embalaje");
                p1.setUbicacion("A-01");
                p1.setStock(150);
                p1.setStockMinimo(20);
                p1.setProveedor("Embalajes S.A.");
                productoRepository.save(p1);

                Producto p2 = new Producto();
                p2.setNombre("Palé de madera");
                p2.setDescripcion("Palé europeo 120x80cm");
                p2.setCategoria("Almacenaje");
                p2.setUbicacion("B-02");
                p2.setStock(30);
                p2.setStockMinimo(10);
                p2.setProveedor("MaderPalés");
                productoRepository.save(p2);

                Producto p3 = new Producto();
                p3.setNombre("Film estirable");
                p3.setDescripcion("Rollo film transparente 500m");
                p3.setCategoria("Embalaje");
                p3.setUbicacion("A-03");
                p3.setStock(8);
                p3.setStockMinimo(10);
                p3.setProveedor("Plásticos Industriales");
                productoRepository.save(p3);

                log.info("Productos de ejemplo creados");
            }

            // Create sample vehicles if none exist
            if (vehiculoRepository.count() == 0) {
                Vehiculo v1 = new Vehiculo();
                v1.setMatricula("1234-ABC");
                v1.setMarca("Renault");
                v1.setModelo("Master");
                v1.setTipo("FURGONETA");
                vehiculoRepository.save(v1);

                Vehiculo v2 = new Vehiculo();
                v2.setMatricula("5678-DEF");
                v2.setMarca("Mercedes");
                v2.setModelo("Sprinter");
                v2.setTipo("FURGONETA");
                vehiculoRepository.save(v2);

                log.info("Vehículos de ejemplo creados");
            }
        };
    }
}
