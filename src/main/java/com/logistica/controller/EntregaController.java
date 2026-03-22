package com.logistica.controller;

import com.logistica.entity.*;
import com.logistica.service.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/entregas")
public class EntregaController {

    private final EntregaService entregaService;
    private final UsuarioService usuarioService;
    private final VehiculoService vehiculoService;
    private final InventarioService inventarioService;

    public EntregaController(EntregaService entregaService, UsuarioService usuarioService,
                             VehiculoService vehiculoService, InventarioService inventarioService) {
        this.entregaService = entregaService;
        this.usuarioService = usuarioService;
        this.vehiculoService = vehiculoService;
        this.inventarioService = inventarioService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'REPARTIDOR')")
    public String list(Model model, @RequestParam(required = false) String estado) {
        List<Entrega> entregas;
        if (estado != null && !estado.isBlank()) {
            try {
                Entrega.Estado estadoEnum = Entrega.Estado.valueOf(estado);
                entregas = entregaService.findByEstado(estadoEnum);
            } catch (IllegalArgumentException e) {
                entregas = entregaService.findAll();
            }
        } else {
            entregas = entregaService.findAll();
        }
        model.addAttribute("entregas", entregas);
        model.addAttribute("estadoFiltro", estado);
        model.addAttribute("estados", Entrega.Estado.values());
        return "entrega/list";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model, Authentication authentication) {
        Entrega entrega = entregaService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entrega no encontrada: " + id));
        model.addAttribute("entrega", entrega);
        model.addAttribute("estados", Entrega.Estado.values());
        return "entrega/detalle";
    }

    @GetMapping("/nueva")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuevaForm(Model model) {
        model.addAttribute("entrega", new Entrega());
        model.addAttribute("clientes", usuarioService.findByRol(Usuario.Rol.CLIENTE));
        model.addAttribute("productos", inventarioService.findProductosActivos());
        model.addAttribute("repartidores", usuarioService.findRepartidoresActivos());
        model.addAttribute("vehiculos", vehiculoService.findDisponibles());
        return "entrega/form";
    }

    @PostMapping("/nueva")
    @PreAuthorize("hasRole('ADMIN')")
    public String crear(@ModelAttribute Entrega entrega,
                        @RequestParam("clienteId") Long clienteId,
                        @RequestParam(value = "repartidorId", required = false) Long repartidorId,
                        @RequestParam(value = "vehiculoId", required = false) Long vehiculoId,
                        @RequestParam(value = "productoIds", required = false) List<Long> productoIds,
                        @RequestParam(value = "cantidades", required = false) List<Integer> cantidades,
                        @RequestParam(value = "fechaEntregaPrevista", required = false) String fechaStr,
                        RedirectAttributes redirectAttributes,
                        Model model) {
        try {
            Usuario cliente = usuarioService.findById(clienteId).orElseThrow();
            entrega.setCliente(cliente);

            if (repartidorId != null) {
                entrega.setRepartidor(usuarioService.findById(repartidorId).orElse(null));
            }
            if (vehiculoId != null) {
                entrega.setVehiculo(vehiculoService.findById(vehiculoId).orElse(null));
            }
            if (fechaStr != null && !fechaStr.isBlank()) {
                entrega.setFechaEntregaPrevista(LocalDate.parse(fechaStr));
            }

            List<EntregaProducto> productos = new java.util.ArrayList<>();
            if (productoIds != null && cantidades != null) {
                for (int i = 0; i < productoIds.size(); i++) {
                    if (productoIds.get(i) != null && cantidades.get(i) != null && cantidades.get(i) > 0) {
                        Producto p = inventarioService.findProductosActivos().stream()
                                .filter(prod -> prod.getId().equals(productoIds.get(i)))
                                .findFirst().orElse(null);
                        if (p != null) {
                            EntregaProducto ep = new EntregaProducto();
                            ep.setProducto(p);
                            ep.setCantidad(cantidades.get(i));
                            productos.add(ep);
                        }
                    }
                }
            }

            if (productos.isEmpty()) {
                model.addAttribute("errorMessage", "Debe agregar al menos un producto.");
                model.addAttribute("entrega", entrega);
                model.addAttribute("clientes", usuarioService.findByRol(Usuario.Rol.CLIENTE));
                model.addAttribute("productos", inventarioService.findProductosActivos());
                model.addAttribute("repartidores", usuarioService.findRepartidoresActivos());
                model.addAttribute("vehiculos", vehiculoService.findDisponibles());
                return "entrega/form";
            }

            entregaService.crearPedido(entrega, productos);
            redirectAttributes.addFlashAttribute("successMessage", "Entrega creada correctamente.");
            return "redirect:/entregas";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear la entrega: " + e.getMessage());
            return "redirect:/entregas/nueva";
        }
    }

    @PostMapping("/{id}/estado")
    public String actualizarEstado(@PathVariable Long id,
                                   @RequestParam("estado") String estado,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        try {
            Entrega.Estado nuevoEstado = Entrega.Estado.valueOf(estado);
            entregaService.actualizarEstado(id, nuevoEstado, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Estado actualizado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/entregas/" + id;
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            entregaService.cancelarEntrega(id);
            redirectAttributes.addFlashAttribute("successMessage", "Entrega cancelada correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/entregas/" + id;
    }

    @PostMapping("/{id}/asignar-repartidor")
    @PreAuthorize("hasRole('ADMIN')")
    public String asignarRepartidor(@PathVariable Long id,
                                    @RequestParam("repartidorId") Long repartidorId,
                                    RedirectAttributes redirectAttributes) {
        try {
            Usuario repartidor = usuarioService.findById(repartidorId).orElseThrow();
            entregaService.asignarRepartidor(id, repartidor);
            redirectAttributes.addFlashAttribute("successMessage", "Repartidor asignado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/entregas/" + id;
    }

    @PostMapping("/{id}/asignar-vehiculo")
    @PreAuthorize("hasRole('ADMIN')")
    public String asignarVehiculo(@PathVariable Long id,
                                  @RequestParam("vehiculoId") Long vehiculoId,
                                  RedirectAttributes redirectAttributes) {
        try {
            Vehiculo vehiculo = vehiculoService.findById(vehiculoId).orElseThrow();
            entregaService.asignarVehiculo(id, vehiculo);
            redirectAttributes.addFlashAttribute("successMessage", "Vehículo asignado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/entregas/" + id;
    }

    @PostMapping("/{id}/modificar-fecha")
    @PreAuthorize("hasRole('ADMIN')")
    public String modificarFecha(@PathVariable Long id,
                                 @RequestParam("fecha") String fechaStr,
                                 RedirectAttributes redirectAttributes) {
        try {
            Entrega entrega = entregaService.findById(id).orElseThrow();
            entrega.setFechaEntregaPrevista(LocalDate.parse(fechaStr));
            entregaService.save(entrega);
            redirectAttributes.addFlashAttribute("successMessage", "Fecha actualizada correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/entregas/" + id;
    }
}
