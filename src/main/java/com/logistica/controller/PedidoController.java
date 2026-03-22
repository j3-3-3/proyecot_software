package com.logistica.controller;

import com.logistica.entity.*;
import com.logistica.service.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/pedidos")
@PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
public class PedidoController {

    private final EntregaService entregaService;
    private final UsuarioService usuarioService;
    private final InventarioService inventarioService;

    public PedidoController(EntregaService entregaService, UsuarioService usuarioService,
                            InventarioService inventarioService) {
        this.entregaService = entregaService;
        this.usuarioService = usuarioService;
        this.inventarioService = inventarioService;
    }

    @GetMapping
    public String misPedidos(Model model, Authentication authentication) {
        Usuario usuario = getUsuario(authentication);
        List<Entrega> pedidos = entregaService.findByCliente(usuario);
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("usuario", usuario);
        return "pedido/list";
    }

    @GetMapping("/nuevo")
    public String nuevoPedidoForm(Model model) {
        model.addAttribute("entrega", new Entrega());
        model.addAttribute("productos", inventarioService.findAll().stream()
                .filter(i -> i.getStockDisponible() > 0)
                .toList());
        return "pedido/form";
    }

    @PostMapping("/nuevo")
    public String crearPedido(@ModelAttribute Entrega entrega,
                              @RequestParam(value = "productoIds", required = false) List<Long> productoIds,
                              @RequestParam(value = "cantidades", required = false) List<Integer> cantidades,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        try {
            Usuario cliente = getUsuario(authentication);
            entrega.setCliente(cliente);

            if (entrega.getDireccionEntrega() == null || entrega.getDireccionEntrega().isBlank()) {
                entrega.setDireccionEntrega(cliente.getDireccion() != null ? cliente.getDireccion() : "Sin dirección");
            }

            List<EntregaProducto> productos = new java.util.ArrayList<>();
            if (productoIds != null && cantidades != null) {
                for (int i = 0; i < productoIds.size(); i++) {
                    if (productoIds.get(i) != null && cantidades.get(i) != null && cantidades.get(i) > 0) {
                        Long productoId = productoIds.get(i);
                        Inventario inv = inventarioService.findByProductoId(productoId).orElse(null);
                        if (inv != null && inv.getStockDisponible() >= cantidades.get(i)) {
                            EntregaProducto ep = new EntregaProducto();
                            ep.setProducto(inv.getProducto());
                            ep.setCantidad(cantidades.get(i));
                            productos.add(ep);
                        } else if (inv != null) {
                            redirectAttributes.addFlashAttribute("errorMessage",
                                    "Stock insuficiente para: " + inv.getProducto().getNombre()
                                    + ". Disponible: " + inv.getStockDisponible());
                            return "redirect:/pedidos/nuevo";
                        }
                    }
                }
            }

            if (productos.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Debe seleccionar al menos un producto.");
                return "redirect:/pedidos/nuevo";
            }

            Entrega pedido = entregaService.crearPedido(entrega, productos);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Pedido creado correctamente. Número: " + pedido.getNumeroPedido());
            return "redirect:/pedidos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear el pedido: " + e.getMessage());
            return "redirect:/pedidos/nuevo";
        }
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model, Authentication authentication) {
        Entrega entrega = entregaService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + id));
        model.addAttribute("entrega", entrega);
        return "pedido/detalle";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        try {
            Entrega entrega = entregaService.findById(id).orElseThrow();
            Usuario usuario = getUsuario(authentication);

            // Clients can only cancel if in EN_PREPARACION or PENDIENTE
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
                Entrega.Estado estado = entrega.getEstado();
                if (estado != Entrega.Estado.EN_PREPARACION && estado != Entrega.Estado.PENDIENTE) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "Solo puede cancelar pedidos en estado 'En Preparación' o 'Pendiente'.");
                    return "redirect:/pedidos/" + id;
                }
                if (!entrega.getCliente().getId().equals(usuario.getId())) {
                    redirectAttributes.addFlashAttribute("errorMessage", "No autorizado.");
                    return "redirect:/pedidos";
                }
            }

            entregaService.cancelarEntrega(id);
            redirectAttributes.addFlashAttribute("successMessage", "Pedido cancelado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/pedidos";
    }

    private Usuario getUsuario(Authentication authentication) {
        return usuarioService.findByUsername(authentication.getName()).orElseThrow();
    }
}
