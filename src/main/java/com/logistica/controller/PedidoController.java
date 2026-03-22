package com.logistica.controller;

import com.logistica.dto.ItemPedidoForm;
import com.logistica.dto.PedidoForm;
import com.logistica.model.Pedido;
import com.logistica.model.Usuario;
import com.logistica.service.EntregaService;
import com.logistica.service.InventarioService;
import com.logistica.service.PedidoService;
import com.logistica.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;
    private final InventarioService inventarioService;
    private final UsuarioService usuarioService;
    private final EntregaService entregaService;

    @GetMapping
    public String lista(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        usuarioService.findByEmail(userDetails.getUsername()).ifPresent(u -> {
            if (u.getRol() == Usuario.Rol.ADMIN) {
                model.addAttribute("pedidos", pedidoService.findAll());
                model.addAttribute("esAdmin", true);
            } else {
                model.addAttribute("pedidos", pedidoService.findByCliente(u));
                model.addAttribute("esAdmin", false);
            }
        });
        return "pedidos/lista";
    }

    @GetMapping("/admin")
    public String adminLista(Model model) {
        model.addAttribute("pedidos", pedidoService.findAll());
        model.addAttribute("esAdmin", true);
        return "pedidos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        PedidoForm form = new PedidoForm();
        List<ItemPedidoForm> items = new ArrayList<>();
        for (int i = 0; i < 5; i++) items.add(new ItemPedidoForm());
        form.setItems(items);
        model.addAttribute("pedidoForm", form);
        model.addAttribute("productosDisponibles", inventarioService.findAll());
        return "pedidos/nuevo";
    }

    @PostMapping("/nuevo")
    public String crearPedido(@Valid @ModelAttribute("pedidoForm") PedidoForm form,
                              BindingResult result,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model, RedirectAttributes redirectAttrs) {
        if (result.hasErrors()) {
            model.addAttribute("productosDisponibles", inventarioService.findAll());
            return "pedidos/nuevo";
        }
        try {
            usuarioService.findByEmail(userDetails.getUsername()).ifPresent(u -> {
                Pedido pedido = pedidoService.crearPedido(form, u);
                redirectAttrs.addFlashAttribute("success", "Pedido #" + pedido.getId() + " creado correctamente.");
            });
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("productosDisponibles", inventarioService.findAll());
            return "pedidos/nuevo";
        }
        return "redirect:/pedidos";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id,
                          @AuthenticationPrincipal UserDetails userDetails,
                          Model model, RedirectAttributes redirectAttrs) {
        return pedidoService.findById(id).map(pedido -> {
            model.addAttribute("pedido", pedido);
            model.addAttribute("total", pedidoService.calcularTotal(pedido));
            usuarioService.findByEmail(userDetails.getUsername()).ifPresent(u -> {
                model.addAttribute("esAdmin", u.getRol() == Usuario.Rol.ADMIN);
                model.addAttribute("esPropietario", pedido.getCliente().getId().equals(u.getId()));
            });
            entregaService.findAll().stream()
                .filter(e -> e.getPedido().getId().equals(id))
                .findFirst()
                .ifPresent(e -> model.addAttribute("entrega", e));
            return "pedidos/detalle";
        }).orElseGet(() -> {
            redirectAttrs.addFlashAttribute("error", "Pedido no encontrado.");
            return "redirect:/pedidos";
        });
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id,
                            @AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes redirectAttrs) {
        pedidoService.findById(id).ifPresent(pedido -> {
            usuarioService.findByEmail(userDetails.getUsername()).ifPresent(u -> {
                boolean esAdmin = u.getRol() == Usuario.Rol.ADMIN;
                boolean esPropietario = pedido.getCliente().getId().equals(u.getId());
                if (esAdmin || esPropietario) {
                    pedidoService.cancelar(id);
                    redirectAttrs.addFlashAttribute("success", "Pedido cancelado correctamente.");
                } else {
                    redirectAttrs.addFlashAttribute("error", "No tienes permiso para cancelar este pedido.");
                }
            });
        });
        return "redirect:/pedidos";
    }

    @PostMapping("/{id}/aprobar")
    public String aprobar(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            entregaService.crearEntregaParaPedido(id);
            redirectAttrs.addFlashAttribute("success", "Pedido aprobado y entrega creada correctamente.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Error al aprobar el pedido: " + e.getMessage());
        }
        return "redirect:/pedidos/" + id;
    }
}
