package com.logistica.controller;

import com.logistica.model.Producto;
import com.logistica.service.InventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    @GetMapping
    public String lista(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("productos", q != null ? inventarioService.search(q) : inventarioService.findAll());
        model.addAttribute("categorias", inventarioService.getCategorias());
        model.addAttribute("q", q);
        return "inventario/lista";
    }

    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", inventarioService.getCategorias());
        model.addAttribute("esNuevo", true);
        return "inventario/form";
    }

    @PostMapping("/nuevo")
    public String guardarNuevo(@Valid @ModelAttribute("producto") Producto producto,
                               BindingResult result, Model model,
                               RedirectAttributes redirectAttrs) {
        if (result.hasErrors()) {
            model.addAttribute("categorias", inventarioService.getCategorias());
            model.addAttribute("esNuevo", true);
            return "inventario/form";
        }
        inventarioService.save(producto);
        redirectAttrs.addFlashAttribute("success", "Producto creado correctamente.");
        return "redirect:/inventario";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model, RedirectAttributes redirectAttrs) {
        return inventarioService.findById(id).map(p -> {
            model.addAttribute("producto", p);
            return "inventario/detalle";
        }).orElseGet(() -> {
            redirectAttrs.addFlashAttribute("error", "Producto no encontrado.");
            return "redirect:/inventario";
        });
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttrs) {
        return inventarioService.findById(id).map(p -> {
            model.addAttribute("producto", p);
            model.addAttribute("categorias", inventarioService.getCategorias());
            model.addAttribute("esNuevo", false);
            return "inventario/form";
        }).orElseGet(() -> {
            redirectAttrs.addFlashAttribute("error", "Producto no encontrado.");
            return "redirect:/inventario";
        });
    }

    @PostMapping("/{id}/editar")
    public String guardarEdicion(@PathVariable Long id,
                                 @Valid @ModelAttribute("producto") Producto producto,
                                 BindingResult result, Model model,
                                 RedirectAttributes redirectAttrs) {
        if (result.hasErrors()) {
            model.addAttribute("categorias", inventarioService.getCategorias());
            model.addAttribute("esNuevo", false);
            return "inventario/form";
        }
        producto.setId(id);
        inventarioService.save(producto);
        redirectAttrs.addFlashAttribute("success", "Producto actualizado correctamente.");
        return "redirect:/inventario/" + id;
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        inventarioService.softDelete(id);
        redirectAttrs.addFlashAttribute("success", "Producto desactivado correctamente.");
        return "redirect:/inventario";
    }
}
