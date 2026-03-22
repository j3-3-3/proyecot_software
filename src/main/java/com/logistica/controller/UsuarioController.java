package com.logistica.controller;

import com.logistica.entity.Usuario;
import com.logistica.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String list(Model model, @RequestParam(required = false) String rol) {
        if (rol != null && !rol.isBlank()) {
            try {
                model.addAttribute("usuarios", usuarioService.findByRol(Usuario.Rol.valueOf(rol)));
            } catch (IllegalArgumentException e) {
                model.addAttribute("usuarios", usuarioService.findAll());
            }
        } else {
            model.addAttribute("usuarios", usuarioService.findAll());
        }
        model.addAttribute("roles", Usuario.Rol.values());
        model.addAttribute("rolFiltro", rol);
        return "usuario/list";
    }

    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", Usuario.Rol.values());
        return "usuario/form";
    }

    @PostMapping("/nuevo")
    public String crear(@Valid @ModelAttribute("usuario") Usuario usuario,
                        BindingResult result,
                        @RequestParam("passwordConfirm") String passwordConfirm,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Usuario.Rol.values());
            return "usuario/form";
        }
        if (usuarioService.existsByUsername(usuario.getUsername())) {
            result.rejectValue("username", "error.usuario", "El username ya está en uso.");
            model.addAttribute("roles", Usuario.Rol.values());
            return "usuario/form";
        }
        if (usuarioService.existsByEmail(usuario.getEmail())) {
            result.rejectValue("email", "error.usuario", "El email ya está registrado.");
            model.addAttribute("roles", Usuario.Rol.values());
            return "usuario/form";
        }
        usuarioService.save(usuario);
        redirectAttributes.addFlashAttribute("successMessage", "Usuario registrado correctamente.");
        return "redirect:/usuarios";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", Usuario.Rol.values());
        return "usuario/editar";
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Long id,
                         @ModelAttribute("usuario") Usuario usuario,
                         @RequestParam(value = "newPassword", required = false) String newPassword,
                         RedirectAttributes redirectAttributes) {
        Usuario existente = usuarioService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        existente.setNombre(usuario.getNombre());
        existente.setApellido(usuario.getApellido());
        existente.setEmail(usuario.getEmail());
        existente.setRol(usuario.getRol());
        existente.setTelefono(usuario.getTelefono());
        existente.setDireccion(usuario.getDireccion());
        existente.setActivo(usuario.isActivo());
        usuarioService.update(existente, newPassword);
        redirectAttributes.addFlashAttribute("successMessage", "Usuario actualizado correctamente.");
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        usuarioService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Usuario desactivado correctamente.");
        return "redirect:/usuarios";
    }
}
