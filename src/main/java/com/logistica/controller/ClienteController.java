package com.logistica.controller;

import com.logistica.model.EstadoPedido;
import com.logistica.service.InventarioService;
import com.logistica.service.PedidoService;
import com.logistica.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cliente")
@RequiredArgsConstructor
public class ClienteController {

    private final PedidoService pedidoService;
    private final InventarioService inventarioService;
    private final UsuarioService usuarioService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        var cliente = usuarioService.findByUsername(userDetails.getUsername());
        var misPedidos = pedidoService.findByCliente(cliente);
        model.addAttribute("misPedidos", misPedidos);
        model.addAttribute("pedidosActivos", misPedidos.stream()
                .filter(p -> p.getEstado() != EstadoPedido.CANCELADO
                        && p.getEstado() != EstadoPedido.ENTREGADO)
                .count());
        return "cliente/dashboard";
    }

    @GetMapping("/mis-pedidos")
    public String misPedidos(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        var cliente = usuarioService.findByUsername(userDetails.getUsername());
        model.addAttribute("pedidos", pedidoService.findByCliente(cliente));
        return "cliente/mis-pedidos";
    }

    @GetMapping("/pedidos/{id}")
    public String verPedido(@PathVariable Long id, Model model) {
        model.addAttribute("pedido", pedidoService.findById(id));
        return "cliente/detalle-pedido";
    }

    @GetMapping("/nuevo-pedido")
    public String nuevoPedidoForm(Model model) {
        model.addAttribute("productos", inventarioService.findAllProductos().stream()
                .filter(p -> p.getStock() > 0).toList());
        return "cliente/nuevo-pedido";
    }

    @PostMapping("/nuevo-pedido")
    public String crearPedido(@RequestParam(required = false) String direccion,
                               @RequestParam(required = false) String observaciones,
                               @RequestParam Map<String, String> params,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes ra) {
        try {
            var cliente = usuarioService.findByUsername(userDetails.getUsername());
            Map<Long, Integer> productoCantidades = new HashMap<>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (entry.getKey().startsWith("cantidad_")) {
                    Long productoId = Long.parseLong(entry.getKey().substring(9));
                    int cantidad = Integer.parseInt(entry.getValue());
                    if (cantidad > 0) {
                        productoCantidades.put(productoId, cantidad);
                    }
                }
            }
            if (productoCantidades.isEmpty()) {
                ra.addFlashAttribute("error", "Debe seleccionar al menos un producto");
                return "redirect:/cliente/nuevo-pedido";
            }
            var pedido = pedidoService.crearPedido(cliente, direccion, observaciones, productoCantidades);
            ra.addFlashAttribute("success", "Pedido #" + pedido.getId() + " creado correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al crear pedido: " + e.getMessage());
        }
        return "redirect:/cliente/mis-pedidos";
    }

    @PostMapping("/pedidos/{id}/cancelar")
    public String cancelarPedido(@PathVariable Long id,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  RedirectAttributes ra) {
        try {
            var pedido = pedidoService.findById(id);
            var cliente = usuarioService.findByUsername(userDetails.getUsername());
            // Only allow cancellation if it belongs to this client and is in EN_PREPARACION
            // or within 24h of creation
            if (!pedido.getCliente().getId().equals(cliente.getId())) {
                throw new IllegalArgumentException("No tienes permisos para cancelar este pedido");
            }
            boolean enPreparacion = pedido.getEstado() == EstadoPedido.EN_PREPARACION;
            boolean dentro24h = pedido.getFechaSolicitud().isAfter(LocalDateTime.now().minusHours(24));
            if (!enPreparacion && !dentro24h) {
                throw new IllegalStateException("No se puede cancelar el pedido en este estado");
            }
            pedidoService.cancelarPedido(id);
            ra.addFlashAttribute("success", "Pedido cancelado correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/cliente/mis-pedidos";
    }
}
