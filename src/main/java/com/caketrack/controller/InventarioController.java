package com.caketrack.controller;

import com.caketrack.model.Ingrediente;
import com.caketrack.model.Producto;
import com.caketrack.model.TipoProducto;
import com.caketrack.repository.IngredienteRepository;
import com.caketrack.repository.ProductoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Controller
public class InventarioController {

    private static final String VISTA_INGREDIENTES = "admin/ingredientes";
    private static final String VISTA_PRODUCTOS = "admin/productos";
    private static final String REDIRECT_INGREDIENTES = "redirect:/admin/ingredientes";
    private static final String REDIRECT_PRODUCTOS = "redirect:/admin/productos";
    private static final String MENSAJE_SIN_INGREDIENTES = "El producto debe tener al menos un ingrediente asociado";

    private final IngredienteRepository ingredienteRepository;
    private final ProductoRepository productoRepository;

    public InventarioController(IngredienteRepository ingredienteRepository, ProductoRepository productoRepository) {
        this.ingredienteRepository = ingredienteRepository;
        this.productoRepository = productoRepository;
    }

    @GetMapping("/admin/ingredientes")
    public String listadoIngredientes(Model model) {
        cargarIngredientes(model);
        return VISTA_INGREDIENTES;
    }

    @GetMapping("/admin/ingredientes/{id}/editar")
    public String editarIngrediente(@PathVariable Long id, Model model) {
        Ingrediente ingrediente = buscarIngrediente(id);
        model.addAttribute("edicion", true);
        model.addAttribute("id", id);
        model.addAttribute("nombre", ingrediente.getNombre());
        model.addAttribute("unidadMedida", ingrediente.getUnidadMedida());
        model.addAttribute("cantidadActual", ingrediente.getCantidadActual());
        model.addAttribute("stockMinimo", ingrediente.getStockMinimo());
        cargarIngredientes(model);
        return VISTA_INGREDIENTES;
    }

    @PostMapping("/admin/ingredientes/crear")
    @Transactional
    public String crearIngrediente(@RequestParam String nombre,
                                   @RequestParam String unidadMedida,
                                   @RequestParam BigDecimal cantidadActual,
                                   @RequestParam BigDecimal stockMinimo,
                                   Model model,
                                   RedirectAttributes redirect) {
        String error = validarIngrediente(nombre, unidadMedida, cantidadActual, stockMinimo, null);
        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("nombre", nombre);
            model.addAttribute("unidadMedida", unidadMedida);
            model.addAttribute("cantidadActual", cantidadActual);
            model.addAttribute("stockMinimo", stockMinimo);
            cargarIngredientes(model);
            return VISTA_INGREDIENTES;
        }
        ingredienteRepository.save(new Ingrediente(nombre.trim(), unidadMedida.trim(), cantidadActual, stockMinimo));
        redirect.addFlashAttribute("mensaje", "Ingrediente registrado exitosamente");
        return REDIRECT_INGREDIENTES;
    }

    @PostMapping("/admin/ingredientes/{id}/editar")
    @Transactional
    public String actualizarIngrediente(@PathVariable Long id,
                                        @RequestParam String nombre,
                                        @RequestParam String unidadMedida,
                                        @RequestParam BigDecimal cantidadActual,
                                        @RequestParam BigDecimal stockMinimo,
                                        Model model,
                                        RedirectAttributes redirect) {
        Ingrediente ingrediente = buscarIngrediente(id);
        String error = validarIngrediente(nombre, unidadMedida, cantidadActual, stockMinimo, id);
        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("edicion", true);
            model.addAttribute("id", id);
            model.addAttribute("nombre", nombre);
            model.addAttribute("unidadMedida", unidadMedida);
            model.addAttribute("cantidadActual", cantidadActual);
            model.addAttribute("stockMinimo", stockMinimo);
            cargarIngredientes(model);
            return VISTA_INGREDIENTES;
        }
        ingrediente.actualizar(nombre.trim(), unidadMedida.trim(), cantidadActual, stockMinimo);
        ingredienteRepository.save(ingrediente);
        redirect.addFlashAttribute("mensaje", "Ingrediente actualizado exitosamente");
        return REDIRECT_INGREDIENTES;
    }

    @GetMapping("/admin/productos")
    public String listadoProductos(Model model) {
        cargarProductos(model);
        return VISTA_PRODUCTOS;
    }

    @PostMapping("/admin/productos/crear")
    @Transactional
    public String crearProducto(@RequestParam String nombre,
                                @RequestParam TipoProducto tipo,
                                @RequestParam BigDecimal precio,
                                @RequestParam(required = false) String descripcion,
                                @RequestParam(name = "ingredienteId", required = false) List<Long> ingredienteIds,
                                @RequestParam(name = "cantidad", required = false) List<BigDecimal> cantidades,
                                Model model,
                                RedirectAttributes redirect) {
        String error = null;
        if (nombre == null || nombre.isBlank()) {
            error = "El nombre es obligatorio";
        } else if (precio.signum() <= 0) {
            error = "El precio debe ser mayor que cero";
        }

        Producto producto = new Producto(nombre == null ? null : nombre.trim(), tipo, precio, descripcion);
        if (error == null) {
            List<Long> ids = ingredienteIds == null ? List.of() : ingredienteIds;
            List<BigDecimal> cants = cantidades == null ? List.of() : cantidades;
            for (int i = 0; i < ids.size() && error == null; i++) {
                Long ingredienteId = ids.get(i);
                BigDecimal cantidad = i < cants.size() ? cants.get(i) : null;
                if (ingredienteId == null || cantidad == null) {
                    continue;
                }
                if (cantidad.signum() <= 0) {
                    error = "La cantidad de cada ingrediente debe ser mayor que cero";
                } else {
                    producto.agregarIngrediente(buscarIngrediente(ingredienteId), cantidad);
                }
            }
            if (error == null && !producto.tieneReceta()) {
                error = MENSAJE_SIN_INGREDIENTES;
            }
        }

        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("nombre", nombre);
            model.addAttribute("tipo", tipo);
            model.addAttribute("precio", precio);
            model.addAttribute("descripcion", descripcion);
            cargarProductos(model);
            return VISTA_PRODUCTOS;
        }
        productoRepository.save(producto);
        redirect.addFlashAttribute("mensaje", "Producto creado exitosamente");
        return REDIRECT_PRODUCTOS;
    }

    @PostMapping("/admin/productos/{id}/disponibilidad")
    @Transactional
    public String cambiarDisponibilidad(@PathVariable Long id, RedirectAttributes redirect) {
        Producto producto = buscarProducto(id);
        if (producto.isDisponible()) {
            producto.marcarNoDisponible();
            redirect.addFlashAttribute("mensaje", "Producto marcado como no disponible");
        } else {
            try {
                producto.marcarDisponible();
                redirect.addFlashAttribute("mensaje", "Producto marcado como disponible");
            } catch (IllegalStateException e) {
                redirect.addFlashAttribute("error", e.getMessage());
            }
        }
        productoRepository.save(producto);
        return REDIRECT_PRODUCTOS;
    }

    @PostMapping("/admin/productos/{id}/receta")
    @Transactional
    public String agregarIngredienteAReceta(@PathVariable Long id,
                                            @RequestParam Long ingredienteId,
                                            @RequestParam BigDecimal cantidad,
                                            RedirectAttributes redirect) {
        Producto producto = buscarProducto(id);
        if (cantidad.signum() <= 0) {
            redirect.addFlashAttribute("error", "La cantidad debe ser mayor que cero");
            return REDIRECT_PRODUCTOS;
        }
        producto.agregarIngrediente(buscarIngrediente(ingredienteId), cantidad);
        productoRepository.save(producto);
        redirect.addFlashAttribute("mensaje", "Receta actualizada");
        return REDIRECT_PRODUCTOS;
    }

    @PostMapping("/admin/productos/{id}/receta/{ingredienteId}/quitar")
    @Transactional
    public String quitarIngredienteDeReceta(@PathVariable Long id,
                                            @PathVariable Long ingredienteId,
                                            RedirectAttributes redirect) {
        Producto producto = buscarProducto(id);
        boolean estabaDisponible = producto.isDisponible();
        producto.quitarIngrediente(buscarIngrediente(ingredienteId));
        productoRepository.save(producto);
        if (estabaDisponible && !producto.isDisponible()) {
            redirect.addFlashAttribute("error", "El producto quedó no disponible porque no tiene ingredientes");
        } else {
            redirect.addFlashAttribute("mensaje", "Ingrediente quitado de la receta");
        }
        return REDIRECT_PRODUCTOS;
    }

    private String validarIngrediente(String nombre, String unidadMedida, BigDecimal cantidadActual,
                                      BigDecimal stockMinimo, Long idActual) {
        if (nombre == null || nombre.isBlank()) {
            return "El nombre es obligatorio";
        }
        if (unidadMedida == null || unidadMedida.isBlank()) {
            return "La unidad de medida es obligatoria";
        }
        if (cantidadActual.signum() < 0 || stockMinimo.signum() < 0) {
            return "Las cantidades no pueden ser negativas";
        }
        boolean nombreUsado = ingredienteRepository.findByNombre(nombre.trim())
                .filter(otro -> !Objects.equals(otro.getId(), idActual))
                .isPresent();
        if (nombreUsado) {
            return "Ya existe un ingrediente con ese nombre";
        }
        return null;
    }

    private Ingrediente buscarIngrediente(Long id) {
        return ingredienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingrediente no encontrado"));
    }

    private Producto buscarProducto(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    private void cargarIngredientes(Model model) {
        model.addAttribute("ingredientes", ingredienteRepository.findAll(Sort.by("nombre")));
    }

    private void cargarProductos(Model model) {
        model.addAttribute("productos", productoRepository.findAll(Sort.by("nombre")));
        model.addAttribute("ingredientes", ingredienteRepository.findAll(Sort.by("nombre")));
        model.addAttribute("tipos", TipoProducto.values());
    }
}
