package com.logistica.controller;

import com.logistica.model.Vehiculo;
import com.logistica.service.VehiculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/vehiculos")
@RequiredArgsConstructor
public class VehiculoController {

    private final VehiculoService vehiculoService;

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("vehiculos", vehiculoService.findAll());
        return "vehiculos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        model.addAttribute("vehiculo", new Vehiculo());
        model.addAttribute("esNuevo", true);
        return "vehiculos/form";
    }

    @PostMapping("/nuevo")
    public String guardarNuevo(@ModelAttribute("vehiculo") Vehiculo vehiculo,
                               RedirectAttributes redirectAttrs) {
        vehiculo.setActivo(true);
        vehiculoService.save(vehiculo);
        redirectAttrs.addFlashAttribute("success", "Vehículo creado correctamente.");
        return "redirect:/vehiculos";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttrs) {
        return vehiculoService.findById(id).map(v -> {
            model.addAttribute("vehiculo", v);
            model.addAttribute("esNuevo", false);
            return "vehiculos/form";
        }).orElseGet(() -> {
            redirectAttrs.addFlashAttribute("error", "Vehículo no encontrado.");
            return "redirect:/vehiculos";
        });
    }

    @PostMapping("/{id}/editar")
    public String guardarEdicion(@PathVariable Long id,
                                 @ModelAttribute("vehiculo") Vehiculo vehiculo,
                                 RedirectAttributes redirectAttrs) {
        vehiculoService.findById(id).ifPresent(existing -> {
            existing.setMatricula(vehiculo.getMatricula());
            existing.setMarca(vehiculo.getMarca());
            existing.setModelo(vehiculo.getModelo());
            existing.setTipo(vehiculo.getTipo());
            existing.setDisponible(vehiculo.isDisponible());
            vehiculoService.save(existing);
        });
        redirectAttrs.addFlashAttribute("success", "Vehículo actualizado correctamente.");
        return "redirect:/vehiculos";
    }
}
