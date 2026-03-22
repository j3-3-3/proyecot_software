package com.logistica.controller;

import com.logistica.model.Producto;
import com.logistica.service.InventarioService;
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

@Controller
@RequestMapping("/operario")
@RequiredArgsConstructor
public class OperarioController {

    private final InventarioService inventarioService;
    private final UsuarioService usuarioService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalProductos", inventarioService.findAllProductos().size());
        model.addAttribute("productosBajoStock", inventarioService.findProductosBajoStock());
        model.addAttribute("ultimosMovimientos", inventarioService.findAllMovimientos().stream().limit(5).toList());
        return "operario/dashboard";
    }

    @GetMapping("/inventario")
    public String inventario(@RequestParam(required = false) String buscar, Model model) {
        model.addAttribute("productos", inventarioService.buscarProductos(buscar));
        model.addAttribute("buscar", buscar);
        return "operario/inventario";
    }

    @GetMapping("/entrada-producto")
    public String entradaProductoForm(Model model) {
        model.addAttribute("productos", inventarioService.findAllProductos());
        return "operario/entrada-producto";
    }

    @PostMapping("/entrada-producto")
    public String registrarEntrada(@RequestParam Long productoId,
                                    @RequestParam Integer cantidad,
                                    @RequestParam(required = false) String lote,
                                    @RequestParam(required = false) String motivo,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes ra) {
        try {
            var usuario = usuarioService.findByUsername(userDetails.getUsername());
            inventarioService.registrarEntrada(productoId, cantidad, lote, motivo, usuario);
            ra.addFlashAttribute("success", "Entrada registrada correctamente. Stock actualizado.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al registrar entrada: " + e.getMessage());
        }
        return "redirect:/operario/entrada-producto";
    }

    @GetMapping("/inventario/{id}/editar")
    public String editarProducto(@PathVariable Long id, Model model) {
        model.addAttribute("producto", inventarioService.findProductoById(id));
        return "operario/editar-producto";
    }

    @PostMapping("/inventario/{id}/ajustar")
    public String ajustarStock(@PathVariable Long id,
                                @RequestParam Integer nuevoStock,
                                @RequestParam(required = false) String motivo,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes ra) {
        try {
            var usuario = usuarioService.findByUsername(userDetails.getUsername());
            inventarioService.ajustarStock(id, nuevoStock, motivo, usuario);
            ra.addFlashAttribute("success", "Stock ajustado correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/operario/inventario";
    }

    @GetMapping("/nuevo-producto")
    public String nuevoProductoForm(Model model) {
        model.addAttribute("producto", new Producto());
        return "operario/nuevo-producto";
    }

    @PostMapping("/nuevo-producto")
    public String guardarProducto(@Valid @ModelAttribute("producto") Producto producto,
                                   BindingResult result,
                                   RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "operario/nuevo-producto";
        }
        inventarioService.guardarProducto(producto);
        ra.addFlashAttribute("success", "Producto creado correctamente");
        return "redirect:/operario/inventario";
    }
}
