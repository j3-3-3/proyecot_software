package com.logistica.controller;

import com.logistica.model.Usuario;
import com.logistica.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UsuarioService usuarioService;
    private final InventarioService inventarioService;
    private final PedidoService pedidoService;
    private final EntregaService entregaService;

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        usuarioService.findByEmail(userDetails.getUsername()).ifPresent(u -> {
            model.addAttribute("usuario", u);
            model.addAttribute("rol", u.getRol().name());

            if (u.getRol() == Usuario.Rol.ADMIN) {
                model.addAttribute("totalProductos", inventarioService.findAll().size());
                model.addAttribute("totalPedidos", pedidoService.findAll().size());
                model.addAttribute("totalEntregas", entregaService.findAll().size());
                model.addAttribute("totalUsuarios", usuarioService.findAll().size());
            } else if (u.getRol() == Usuario.Rol.OPERARIO) {
                model.addAttribute("totalProductos", inventarioService.findAll().size());
                model.addAttribute("categorias", inventarioService.getCategorias().size());
            } else if (u.getRol() == Usuario.Rol.REPARTIDOR) {
                model.addAttribute("misEntregas", entregaService.findByRepartidor(u).size());
            } else if (u.getRol() == Usuario.Rol.CLIENTE) {
                model.addAttribute("misPedidos", pedidoService.findByCliente(u).size());
            }
        });
        return "dashboard";
    }
}
