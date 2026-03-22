package com.logistica.service;

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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        if (!usuario.isActivo()) {
            throw new UsernameNotFoundException("Usuario desactivado: " + email);
        }

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                .authorities(Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name())
                ))
                .build();
    }

    @Transactional(readOnly = true)
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<Usuario> findByRol(Usuario.Rol rol) {
        return usuarioRepository.findByRolAndActivoTrue(rol);
    }

    @Transactional
    public Usuario save(Usuario usuario, boolean encodePassword) {
        if (encodePassword && usuario.getPassword() != null && !usuario.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void deactivate(Long id) {
        usuarioRepository.findById(id).ifPresent(u -> {
            u.setActivo(false);
            usuarioRepository.save(u);
        });
    }

    public boolean existsByEmail(String email) {
        return usuarioRepository.findByEmail(email).isPresent();
    }
}
