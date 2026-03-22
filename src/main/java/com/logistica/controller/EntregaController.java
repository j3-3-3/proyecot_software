package com.logistica.controller;

import com.logistica.dto.ActualizarEstadoForm;
import com.logistica.dto.AsignarEntregaForm;
import com.logistica.model.Entrega;
import com.logistica.model.Usuario;
import com.logistica.service.EntregaService;
import com.logistica.service.UsuarioService;
import com.logistica.service.VehiculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/entregas")
@RequiredArgsConstructor
public class EntregaController {

    private final EntregaService entregaService;
    private final UsuarioService usuarioService;
    private final VehiculoService vehiculoService;

    @GetMapping
    public String lista(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        usuarioService.findByEmail(userDetails.getUsername()).ifPresent(u -> {
            if (u.getRol() == Usuario.Rol.ADMIN) {
                model.addAttribute("entregas", entregaService.findAll());
                model.addAttribute("esAdmin", true);
            } else {
                model.addAttribute("entregas", entregaService.findByRepartidor(u));
                model.addAttribute("esAdmin", false);
            }
        });
        return "entregas/lista";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model, RedirectAttributes redirectAttrs) {
        return entregaService.findById(id).map(e -> {
            model.addAttribute("entrega", e);
            return "entregas/detalle";
        }).orElseGet(() -> {
            redirectAttrs.addFlashAttribute("error", "Entrega no encontrada.");
            return "redirect:/entregas";
        });
    }

    @GetMapping("/{id}/asignar")
    public String asignarForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttrs) {
        return entregaService.findById(id).map(e -> {
            model.addAttribute("entrega", e);
            model.addAttribute("form", new AsignarEntregaForm());
            model.addAttribute("repartidores", usuarioService.findByRol(Usuario.Rol.REPARTIDOR));
            model.addAttribute("vehiculos", vehiculoService.findDisponibles());
            return "entregas/asignar";
        }).orElseGet(() -> {
            redirectAttrs.addFlashAttribute("error", "Entrega no encontrada.");
            return "redirect:/entregas";
        });
    }

    @PostMapping("/{id}/asignar")
    public String asignar(@PathVariable Long id, @ModelAttribute AsignarEntregaForm form,
                           RedirectAttributes redirectAttrs) {
        try {
            entregaService.asignar(id, form);
            redirectAttrs.addFlashAttribute("success", "Entrega asignada correctamente.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Error al asignar: " + e.getMessage());
        }
        return "redirect:/entregas/" + id;
    }

    @GetMapping("/{id}/estado")
    public String estadoForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttrs) {
        return entregaService.findById(id).map(e -> {
            model.addAttribute("entrega", e);
            model.addAttribute("form", new ActualizarEstadoForm());
            model.addAttribute("estados", Entrega.EstadoEntrega.values());
            return "entregas/estado";
        }).orElseGet(() -> {
            redirectAttrs.addFlashAttribute("error", "Entrega no encontrada.");
            return "redirect:/entregas";
        });
    }

    @PostMapping("/{id}/estado")
    public String actualizarEstado(@PathVariable Long id,
                                    @ModelAttribute ActualizarEstadoForm form,
                                    RedirectAttributes redirectAttrs) {
        try {
            entregaService.actualizarEstado(id, form);
            redirectAttrs.addFlashAttribute("success", "Estado actualizado correctamente.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Error al actualizar estado: " + e.getMessage());
        }
        return "redirect:/entregas/" + id;
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        entregaService.cancelar(id);
        redirectAttrs.addFlashAttribute("success", "Entrega cancelada correctamente.");
        return "redirect:/entregas";
    }
}
