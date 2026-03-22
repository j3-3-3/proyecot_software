package com.logistica.controller;

import com.logistica.model.EstadoEntrega;
import com.logistica.service.EntregaService;
import com.logistica.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/repartidor")
@RequiredArgsConstructor
public class RepartidorController {

    private final EntregaService entregaService;
    private final UsuarioService usuarioService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        var repartidor = usuarioService.findByUsername(userDetails.getUsername());
        var misEntregas = entregaService.findByRepartidor(repartidor);
        model.addAttribute("misEntregas", misEntregas);
        model.addAttribute("entregasActivas", misEntregas.stream()
                .filter(e -> e.getEstado() != EstadoEntrega.ENTREGADO
                        && e.getEstado() != EstadoEntrega.CANCELADO)
                .count());
        return "repartidor/dashboard";
    }

    @GetMapping("/mis-entregas")
    public String misEntregas(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        var repartidor = usuarioService.findByUsername(userDetails.getUsername());
        model.addAttribute("entregas", entregaService.findByRepartidor(repartidor));
        model.addAttribute("estadosDisponibles", EstadoEntrega.values());
        return "repartidor/mis-entregas";
    }

    @GetMapping("/entregas/{id}")
    public String verEntrega(@PathVariable Long id, Model model) {
        model.addAttribute("entrega", entregaService.findById(id));
        model.addAttribute("estadosDisponibles", EstadoEntrega.values());
        return "repartidor/detalle-entrega";
    }

    @PostMapping("/entregas/{id}/actualizar-estado")
    public String actualizarEstado(@PathVariable Long id,
                                    @RequestParam EstadoEntrega nuevoEstado,
                                    @RequestParam(required = false) String observaciones,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes ra) {
        try {
            entregaService.actualizarEstado(id, nuevoEstado, observaciones);
            ra.addFlashAttribute("success", "Estado actualizado a: " + nuevoEstado.name().replace("_", " "));
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/repartidor/mis-entregas";
    }
}
