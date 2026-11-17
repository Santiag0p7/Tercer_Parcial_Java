package com.caketrack.controller;

import com.caketrack.model.Ingrediente;
import com.caketrack.model.Producto;
import com.caketrack.repository.IngredienteRepository;
import com.caketrack.repository.ProductoRepository;
import com.caketrack.repository.UsuarioRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Comparator;
import java.util.List;

@Controller
public class HomeController {

    private final ProductoRepository productoRepository;
    private final IngredienteRepository ingredienteRepository;
    private final UsuarioRepository usuarioRepository;

    public HomeController(ProductoRepository productoRepository,
                          IngredienteRepository ingredienteRepository,
                          UsuarioRepository usuarioRepository) {
        this.productoRepository = productoRepository;
        this.ingredienteRepository = ingredienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/")
    public String landing(Authentication authentication, Model model) {
        if (estaAutenticado(authentication)) {
            return "redirect:/inicio";
        }
        cargarLanding(model);
        return "landing";
    }

    @GetMapping("/login")
    public String login(Authentication authentication, Model model) {
        if (estaAutenticado(authentication)) {
            return "redirect:/inicio";
        }
        cargarLanding(model);
        return "landing";
    }

    @GetMapping("/inicio")
    public String inicio(Model model) {
        model.addAttribute("totalUsuarios", usuarioRepository.count());
        model.addAttribute("ingredientesEnAlerta", ingredienteRepository.findAll().stream()
                .filter(Ingrediente::isBajoStockMinimo)
                .count());
        model.addAttribute("productosDisponibles", productoRepository.findByDisponibleTrue().stream()
                .filter(Producto::isDisponibleReal)
                .count());
        return "inicio";
    }

    private void cargarLanding(Model model) {
        List<Producto> productos = productoRepository.findByDisponibleTrue().stream()
                .filter(Producto::isDisponibleReal)
                .sorted(Comparator.comparing(Producto::getNombre))
                .limit(6)
                .toList();
        model.addAttribute("productos", productos);
    }

    private boolean estaAutenticado(Authentication authentication) {
        return authentication != null && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
