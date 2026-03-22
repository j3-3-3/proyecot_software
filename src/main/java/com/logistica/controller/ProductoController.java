package com.logistica.controller;

import com.logistica.entity.Producto;
import com.logistica.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/productos")
@PreAuthorize("hasAnyRole('ADMIN', 'OPERARIO')")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public String list(Model model, @RequestParam(required = false) String busqueda) {
        if (busqueda != null && !busqueda.isBlank()) {
            model.addAttribute("productos", productoService.buscarPorNombre(busqueda));
        } else {
            model.addAttribute("productos", productoService.findAll());
        }
        model.addAttribute("busqueda", busqueda);
        return "productos/list";
    }

    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        model.addAttribute("producto", new Producto());
        return "productos/form";
    }

    @PostMapping("/nuevo")
    public String crear(@Valid @ModelAttribute("producto") Producto producto,
                        BindingResult result,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "productos/form";
        }
        productoService.save(producto);
        redirectAttributes.addFlashAttribute("successMessage", "Producto creado correctamente.");
        return "redirect:/productos";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        Producto producto = productoService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + id));
        model.addAttribute("producto", producto);
        return "productos/form";
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Long id,
                         @Valid @ModelAttribute("producto") Producto producto,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "productos/form";
        }
        producto.setId(id);
        productoService.save(producto);
        redirectAttributes.addFlashAttribute("successMessage", "Producto actualizado correctamente.");
        return "redirect:/productos";
    }

    @PostMapping("/{id}/eliminar")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productoService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Producto eliminado correctamente.");
        return "redirect:/productos";
    }
}
