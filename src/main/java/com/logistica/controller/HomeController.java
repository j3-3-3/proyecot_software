package com.logistica.controller;

import com.logistica.entity.Entrega;
import com.logistica.entity.Usuario;
import com.logistica.service.EntregaService;
import com.logistica.service.InventarioService;
import com.logistica.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final UsuarioService usuarioService;
    private final EntregaService entregaService;
    private final InventarioService inventarioService;

    public HomeController(UsuarioService usuarioService, EntregaService entregaService,
                          InventarioService inventarioService) {
        this.usuarioService = usuarioService;
        this.entregaService = entregaService;
        this.inventarioService = inventarioService;
    }

    @GetMapping("/")
    public String root(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        return switch (role) {
            case "ROLE_ADMIN" -> "redirect:/admin/dashboard";
            case "ROLE_OPERARIO" -> "redirect:/operario/dashboard";
            case "ROLE_REPARTIDOR" -> "redirect:/repartidor/dashboard";
            case "ROLE_CLIENTE" -> "redirect:/cliente/dashboard";
            default -> "redirect:/login";
        };
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model, Authentication authentication) {
        Usuario usuario = getCurrentUser(authentication);
        model.addAttribute("usuario", usuario);
        model.addAttribute("totalUsuarios", usuarioService.findAll().size());
        model.addAttribute("totalEntregas", entregaService.findAll().size());
        model.addAttribute("entregasPendientes", entregaService.findByEstado(Entrega.Estado.PENDIENTE).size());
        model.addAttribute("entregasEnReparto", entregaService.findByEstado(Entrega.Estado.EN_REPARTO).size());
        model.addAttribute("productosConBajoStock", inventarioService.findProductosBajoStock().size());
        model.addAttribute("ultimasEntregas", entregaService.findAll().stream()
                .sorted((a, b) -> b.getFechaPedido() != null && a.getFechaPedido() != null
                        ? b.getFechaPedido().compareTo(a.getFechaPedido()) : 0)
                .limit(5)
                .toList());
        return "home/admin-dashboard";
    }

    @GetMapping("/operario/dashboard")
    public String operarioDashboard(Model model, Authentication authentication) {
        Usuario usuario = getCurrentUser(authentication);
        model.addAttribute("usuario", usuario);
        model.addAttribute("totalInventario", inventarioService.findAll().size());
        model.addAttribute("productosConBajoStock", inventarioService.findProductosBajoStock().size());
        model.addAttribute("inventario", inventarioService.findAll());
        return "home/operario-dashboard";
    }

    @GetMapping("/repartidor/dashboard")
    public String repartidorDashboard(Model model, Authentication authentication) {
        Usuario usuario = getCurrentUser(authentication);
        model.addAttribute("usuario", usuario);
        model.addAttribute("misEntregas", entregaService.findByRepartidor(usuario));
        model.addAttribute("entregasPendientes",
                entregaService.findByRepartidor(usuario).stream()
                        .filter(e -> e.getEstado() == Entrega.Estado.PENDIENTE
                                || e.getEstado() == Entrega.Estado.EN_REPARTO)
                        .toList());
        return "home/repartidor-dashboard";
    }

    @GetMapping("/cliente/dashboard")
    public String clienteDashboard(Model model, Authentication authentication) {
        Usuario usuario = getCurrentUser(authentication);
        model.addAttribute("usuario", usuario);
        model.addAttribute("misPedidos", entregaService.findByCliente(usuario));
        return "home/cliente-dashboard";
    }

    private Usuario getCurrentUser(Authentication authentication) {
        return usuarioService.findByUsername(authentication.getName()).orElseThrow();
    }
}
