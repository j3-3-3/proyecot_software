package com.logistica.config;

import com.logistica.entity.Usuario;
import com.logistica.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Create default admin if it doesn't exist
        if (!usuarioRepository.existsByUsername("admin")) {
            Usuario admin = new Usuario();
            admin.setNombre("Administrador");
            admin.setApellido("Sistema");
            admin.setEmail("admin@logistica.com");
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("Admin1234"));
            admin.setRol(Usuario.Rol.ADMIN);
            admin.setActivo(true);
            usuarioRepository.save(admin);
            System.out.println("Usuario admin creado: admin / Admin1234");
        }

        // Create default operario
        if (!usuarioRepository.existsByUsername("operario1")) {
            Usuario operario = new Usuario();
            operario.setNombre("Carlos");
            operario.setApellido("Garcia");
            operario.setEmail("operario@logistica.com");
            operario.setUsername("operario1");
            operario.setPassword(passwordEncoder.encode("Operario1234"));
            operario.setRol(Usuario.Rol.OPERARIO);
            operario.setActivo(true);
            usuarioRepository.save(operario);
        }

        // Create default repartidor
        if (!usuarioRepository.existsByUsername("repartidor1")) {
            Usuario repartidor = new Usuario();
            repartidor.setNombre("Maria");
            repartidor.setApellido("Lopez");
            repartidor.setEmail("repartidor@logistica.com");
            repartidor.setUsername("repartidor1");
            repartidor.setPassword(passwordEncoder.encode("Repartidor1234"));
            repartidor.setRol(Usuario.Rol.REPARTIDOR);
            repartidor.setActivo(true);
            usuarioRepository.save(repartidor);
        }

        // Create default cliente
        if (!usuarioRepository.existsByUsername("cliente1")) {
            Usuario cliente = new Usuario();
            cliente.setNombre("Juan");
            cliente.setApellido("Perez");
            cliente.setEmail("cliente@logistica.com");
            cliente.setUsername("cliente1");
            cliente.setPassword(passwordEncoder.encode("Cliente1234"));
            cliente.setRol(Usuario.Rol.CLIENTE);
            cliente.setActivo(true);
            cliente.setDireccion("Calle Mayor 123, Teruel");
            usuarioRepository.save(cliente);
        }
    }
}
