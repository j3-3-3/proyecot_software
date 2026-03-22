package com.logistica.controller;

import com.logistica.model.*;
import com.logistica.service.EntregaService;
import com.logistica.service.InventarioService;
import com.logistica.service.PedidoService;
import com.logistica.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UsuarioService usuarioService;
    private final InventarioService inventarioService;
    private final PedidoService pedidoService;
    private final EntregaService entregaService;

    // ===== DASHBOARD =====
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalEmpleados", usuarioService.findAll().stream()
                .filter(u -> u.getRol() != Rol.CLIENTE).count());
        model.addAttribute("totalProductos", inventarioService.findAllProductos().size());
        model.addAttribute("totalPedidos", pedidoService.findAll().size());
        model.addAttribute("totalEntregas", entregaService.findAll().size());
        model.addAttribute("productosBajoStock", inventarioService.findProductosBajoStock());
        model.addAttribute("ultimasEntregas", entregaService.findAll().stream().limit(5).toList());
        return "admin/dashboard";
    }

    // ===== EMPLEADOS =====
    @GetMapping("/empleados")
    public String empleados(Model model) {
        model.addAttribute("empleados", usuarioService.findAll().stream()
                .filter(u -> u.getRol() != Rol.CLIENTE && u.isActivo()).toList());
        return "admin/empleados";
    }

    @GetMapping("/empleados/nuevo")
    public String nuevoEmpleadoForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", new Rol[]{Rol.OPERARIO, Rol.REPARTIDOR, Rol.ADMIN});
        return "admin/empleado-form";
    }

    @PostMapping("/empleados/nuevo")
    public String guardarEmpleado(@RequestParam String username,
                                   @RequestParam String password,
                                   @RequestParam String nombre,
                                   @RequestParam String email,
                                   @RequestParam Rol rol,
                                   RedirectAttributes ra) {
        try {
            usuarioService.registrarUsuario(username, password, nombre, email, rol);
            ra.addFlashAttribute("success", "Empleado registrado correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al registrar empleado: " + e.getMessage());
        }
        return "redirect:/admin/empleados";
    }

    @PostMapping("/empleados/{id}/eliminar")
    public String eliminarEmpleado(@PathVariable Long id, RedirectAttributes ra) {
        try {
            usuarioService.eliminarUsuario(id);
            ra.addFlashAttribute("success", "Empleado desactivado correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/empleados";
    }

    // ===== INVENTARIO =====
    @GetMapping("/inventario")
    public String inventario(@RequestParam(required = false) String buscar, Model model) {
        model.addAttribute("productos", inventarioService.buscarProductos(buscar));
        model.addAttribute("buscar", buscar);
        model.addAttribute("movimientos", inventarioService.findAllMovimientos().stream().limit(10).toList());
        return "admin/inventario";
    }

    @GetMapping("/inventario/nuevo")
    public String nuevoProductoForm(Model model) {
        model.addAttribute("producto", new Producto());
        return "admin/producto-form";
    }

    @PostMapping("/inventario/nuevo")
    public String guardarProducto(@Valid @ModelAttribute("producto") Producto producto,
                                   BindingResult result,
                                   RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "admin/producto-form";
        }
        inventarioService.guardarProducto(producto);
        ra.addFlashAttribute("success", "Producto guardado correctamente");
        return "redirect:/admin/inventario";
    }

    @GetMapping("/inventario/{id}/editar")
    public String editarProductoForm(@PathVariable Long id, Model model) {
        model.addAttribute("producto", inventarioService.findProductoById(id));
        return "admin/producto-form";
    }

    @PostMapping("/inventario/{id}/editar")
    public String actualizarProducto(@PathVariable Long id,
                                      @Valid @ModelAttribute("producto") Producto producto,
                                      BindingResult result,
                                      RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "admin/producto-form";
        }
        producto.setId(id);
        inventarioService.guardarProducto(producto);
        ra.addFlashAttribute("success", "Producto actualizado correctamente");
        return "redirect:/admin/inventario";
    }

    // ===== PEDIDOS =====
    @GetMapping("/pedidos")
    public String pedidos(Model model) {
        model.addAttribute("pedidos", pedidoService.findAll());
        return "admin/pedidos";
    }

    @PostMapping("/pedidos/{id}/cancelar")
    public String cancelarPedido(@PathVariable Long id, RedirectAttributes ra) {
        try {
            pedidoService.cancelarPedido(id);
            ra.addFlashAttribute("success", "Pedido cancelado correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/pedidos";
    }

    // ===== ENTREGAS =====
    @GetMapping("/entregas")
    public String entregas(Model model) {
        model.addAttribute("entregas", entregaService.findAll());
        model.addAttribute("repartidores", usuarioService.findByRol(Rol.REPARTIDOR));
        model.addAttribute("vehiculos", entregaService.findVehiculosDisponibles());
        return "admin/entregas";
    }

    @GetMapping("/entregas/nueva")
    public String nuevaEntregaForm(Model model) {
        model.addAttribute("pedidos", pedidoService.findAll().stream()
                .filter(p -> p.getEntrega() == null && p.getEstado() != EstadoPedido.CANCELADO)
                .toList());
        return "admin/entrega-form";
    }

    @PostMapping("/entregas/nueva")
    public String crearEntrega(@RequestParam Long pedidoId,
                                @RequestParam String fechaPrevista,
                                RedirectAttributes ra) {
        try {
            Pedido pedido = pedidoService.findById(pedidoId);
            entregaService.crearEntrega(pedido, LocalDate.parse(fechaPrevista));
            ra.addFlashAttribute("success", "Entrega creada correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/entregas";
    }

    @PostMapping("/entregas/{id}/asignar-repartidor")
    public String asignarRepartidor(@PathVariable Long id,
                                     @RequestParam Long repartidorId,
                                     RedirectAttributes ra) {
        try {
            Usuario repartidor = usuarioService.findById(repartidorId);
            entregaService.asignarRepartidor(id, repartidor);
            ra.addFlashAttribute("success", "Repartidor asignado correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/entregas";
    }

    @PostMapping("/entregas/{id}/asignar-vehiculo")
    public String asignarVehiculo(@PathVariable Long id,
                                   @RequestParam Long vehiculoId,
                                   RedirectAttributes ra) {
        try {
            entregaService.asignarVehiculo(id, vehiculoId);
            ra.addFlashAttribute("success", "Vehículo asignado correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/entregas";
    }

    @PostMapping("/entregas/{id}/modificar-fecha")
    public String modificarFechaEntrega(@PathVariable Long id,
                                         @RequestParam String nuevaFecha,
                                         RedirectAttributes ra) {
        try {
            entregaService.modificarFecha(id, LocalDate.parse(nuevaFecha));
            ra.addFlashAttribute("success", "Fecha modificada correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/entregas";
    }

    @PostMapping("/entregas/{id}/cancelar")
    public String cancelarEntrega(@PathVariable Long id, RedirectAttributes ra) {
        try {
            entregaService.actualizarEstado(id, EstadoEntrega.CANCELADO, "Cancelado por administrador");
            ra.addFlashAttribute("success", "Entrega cancelada correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/entregas";
    }

    // ===== VEHÍCULOS =====
    @GetMapping("/vehiculos")
    public String vehiculos(Model model) {
        model.addAttribute("vehiculos", entregaService.findAllVehiculos());
        return "admin/vehiculos";
    }

    @GetMapping("/vehiculos/nuevo")
    public String nuevoVehiculoForm(Model model) {
        model.addAttribute("vehiculo", new Vehiculo());
        return "admin/vehiculo-form";
    }

    @PostMapping("/vehiculos/nuevo")
    public String guardarVehiculo(@Valid @ModelAttribute("vehiculo") Vehiculo vehiculo,
                                   BindingResult result,
                                   RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "admin/vehiculo-form";
        }
        entregaService.guardarVehiculo(vehiculo);
        ra.addFlashAttribute("success", "Vehículo registrado correctamente");
        return "redirect:/admin/vehiculos";
    }
}
