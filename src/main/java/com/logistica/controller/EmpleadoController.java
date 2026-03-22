package com.logistica.controller;

import com.logistica.model.Usuario;
import com.logistica.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/empleados")
@RequiredArgsConstructor
public class EmpleadoController {

    private final UsuarioService usuarioService;

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("usuarios", usuarioService.findAll());
        return "empleados/lista";
    }

    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", Usuario.Rol.values());
        model.addAttribute("esNuevo", true);
        return "empleados/form";
    }

    @PostMapping("/nuevo")
    public String guardarNuevo(@ModelAttribute("usuario") Usuario usuario,
                               RedirectAttributes redirectAttrs) {
        if (usuarioService.existsByEmail(usuario.getEmail())) {
            redirectAttrs.addFlashAttribute("error", "Ya existe un usuario con ese correo electrónico.");
            return "redirect:/empleados/nuevo";
        }
        usuario.setActivo(true);
        usuarioService.save(usuario, true);
        redirectAttrs.addFlashAttribute("success", "Empleado creado correctamente.");
        return "redirect:/empleados";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttrs) {
        return usuarioService.findById(id).map(u -> {
            u.setPassword("");
            model.addAttribute("usuario", u);
            model.addAttribute("roles", Usuario.Rol.values());
            model.addAttribute("esNuevo", false);
            return "empleados/form";
        }).orElseGet(() -> {
            redirectAttrs.addFlashAttribute("error", "Usuario no encontrado.");
            return "redirect:/empleados";
        });
    }

    @PostMapping("/{id}/editar")
    public String guardarEdicion(@PathVariable Long id,
                                 @ModelAttribute("usuario") Usuario usuario,
                                 RedirectAttributes redirectAttrs) {
        usuarioService.findById(id).ifPresent(existing -> {
            existing.setNombre(usuario.getNombre());
            existing.setApellidos(usuario.getApellidos());
            existing.setEmail(usuario.getEmail());
            existing.setRol(usuario.getRol());
            existing.setActivo(usuario.isActivo());
            boolean encodePassword = usuario.getPassword() != null && !usuario.getPassword().isBlank();
            if (encodePassword) existing.setPassword(usuario.getPassword());
            usuarioService.save(existing, encodePassword);
        });
        redirectAttrs.addFlashAttribute("success", "Empleado actualizado correctamente.");
        return "redirect:/empleados";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        usuarioService.deactivate(id);
        redirectAttrs.addFlashAttribute("success", "Empleado desactivado correctamente.");
        return "redirect:/empleados";
    }
}
