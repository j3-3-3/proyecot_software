package com.logistica.service;

import com.logistica.model.Rol;
import com.logistica.model.Usuario;
import com.logistica.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        if (!usuario.isActivo()) {
            throw new UsernameNotFoundException("Usuario desactivado: " + username);
        }

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()))
                .build();
    }

    @Transactional
    public Usuario registrarUsuario(String username, String password, String nombre, String email, Rol rol) {
        if (usuarioRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("El nombre de usuario ya existe: " + username);
        }
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setRol(rol);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    public Usuario findByUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public List<Usuario> findByRol(Rol rol) {
        return usuarioRepository.findByRol(rol);
    }

    public Usuario findById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
    }
}
