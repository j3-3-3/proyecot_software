package com.logistica.controller;

import com.logistica.entity.Vehiculo;
import com.logistica.service.VehiculoService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/vehiculos")
@PreAuthorize("hasRole('ADMIN')")
public class VehiculoController {

    private final VehiculoService vehiculoService;

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("vehiculos", vehiculoService.findAll());
        return "vehiculo/list";
    }

    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        model.addAttribute("vehiculo", new Vehiculo());
        return "vehiculo/form";
    }

    @PostMapping("/nuevo")
    public String crear(@Valid @ModelAttribute("vehiculo") Vehiculo vehiculo,
                        BindingResult result,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "vehiculo/form";
        }
        if (vehiculoService.existsByMatricula(vehiculo.getMatricula())) {
            result.rejectValue("matricula", "error.vehiculo", "Ya existe un vehículo con esa matrícula.");
            return "vehiculo/form";
        }
        vehiculoService.save(vehiculo);
        redirectAttributes.addFlashAttribute("successMessage", "Vehículo registrado correctamente.");
        return "redirect:/vehiculos";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        Vehiculo vehiculo = vehiculoService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado: " + id));
        model.addAttribute("vehiculo", vehiculo);
        return "vehiculo/form";
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Long id,
                         @Valid @ModelAttribute("vehiculo") Vehiculo vehiculo,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "vehiculo/form";
        }
        vehiculo.setId(id);
        vehiculoService.save(vehiculo);
        redirectAttributes.addFlashAttribute("successMessage", "Vehículo actualizado correctamente.");
        return "redirect:/vehiculos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        vehiculoService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Vehículo eliminado correctamente.");
        return "redirect:/vehiculos";
    }
}
