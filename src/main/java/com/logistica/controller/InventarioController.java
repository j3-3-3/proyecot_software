package com.logistica.controller;

import com.logistica.entity.Inventario;
import com.logistica.entity.Producto;
import com.logistica.service.InventarioService;
import com.logistica.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/inventario")
@PreAuthorize("hasAnyRole('ADMIN', 'OPERARIO')")
public class InventarioController {

    private final InventarioService inventarioService;
    private final ProductoService productoService;

    public InventarioController(InventarioService inventarioService, ProductoService productoService) {
        this.inventarioService = inventarioService;
        this.productoService = productoService;
    }

    @GetMapping
    public String list(Model model, @RequestParam(required = false) String busqueda) {
        List<Inventario> inventario;
        if (busqueda != null && !busqueda.isBlank()) {
            inventario = inventarioService.findAll().stream()
                    .filter(i -> i.getProducto().getNombre().toLowerCase().contains(busqueda.toLowerCase())
                            || i.getProducto().getCategoria().toLowerCase().contains(busqueda.toLowerCase())
                            || i.getUbicacion().toLowerCase().contains(busqueda.toLowerCase()))
                    .toList();
        } else {
            inventario = inventarioService.findAll();
        }
        model.addAttribute("inventario", inventario);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("alertasBajoStock", inventarioService.findProductosBajoStock());
        return "inventario/list";
    }

    @GetMapping("/entrada")
    public String entradaForm(Model model) {
        model.addAttribute("inventario", new Inventario());
        model.addAttribute("productos", productoService.findActivos());
        return "inventario/entrada";
    }

    @PostMapping("/entrada")
    public String registrarEntrada(@Valid @ModelAttribute("inventario") Inventario inventario,
                                   BindingResult result,
                                   @RequestParam("productoId") Long productoId,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("productos", productoService.findActivos());
            return "inventario/entrada";
        }
        Producto producto = productoService.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        inventario.setProducto(producto);
        inventarioService.registrarEntrada(inventario);
        redirectAttributes.addFlashAttribute("successMessage", "Entrada de producto registrada correctamente.");
        return "redirect:/inventario";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        Inventario inventario = inventarioService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventario no encontrado: " + id));
        model.addAttribute("inventario", inventario);
        model.addAttribute("productos", productoService.findActivos());
        return "inventario/editar";
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Long id,
                         @Valid @ModelAttribute("inventario") Inventario inventario,
                         BindingResult result,
                         @RequestParam("productoId") Long productoId,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("productos", productoService.findActivos());
            return "inventario/editar";
        }
        Inventario existente = inventarioService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventario no encontrado"));
        existente.setCantidad(inventario.getCantidad());
        existente.setUbicacion(inventario.getUbicacion());
        existente.setMotivoUltimoCambio(inventario.getMotivoUltimoCambio());
        if (inventario.getNumeroLote() != null) existente.setNumeroLote(inventario.getNumeroLote());
        inventarioService.modificar(existente);
        redirectAttributes.addFlashAttribute("successMessage", "Inventario actualizado correctamente.");
        return "redirect:/inventario";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        Inventario inventario = inventarioService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventario no encontrado: " + id));
        model.addAttribute("inventario", inventario);
        return "inventario/detalle";
    }
}
