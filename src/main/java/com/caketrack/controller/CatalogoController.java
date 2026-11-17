package com.caketrack.controller;

import com.caketrack.model.Producto;
import com.caketrack.model.TipoProducto;
import com.caketrack.repository.ProductoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;

@Controller
public class CatalogoController {

    private final ProductoRepository productoRepository;

    public CatalogoController(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @GetMapping("/catalogo")
    public String catalogo(@RequestParam(required = false) TipoProducto tipo, Model model) {
        List<Producto> productos = productoRepository.findByDisponibleTrue().stream()
                .filter(Producto::isDisponibleReal)
                .filter(producto -> tipo == null || producto.getTipo() == tipo)
                .sorted(Comparator.comparing(Producto::getNombre))
                .toList();
        model.addAttribute("productos", productos);
        model.addAttribute("tipos", TipoProducto.values());
        model.addAttribute("tipoSeleccionado", tipo);
        return "catalogo";
    }
}
