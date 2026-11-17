package com.caketrack.controller;

import com.caketrack.model.Rol;
import com.caketrack.model.Usuario;
import com.caketrack.repository.UsuarioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/admin/usuarios")
public class AdminUsuarioController {

    private static final Pattern CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int LONGITUD_MINIMA_PASSWORD = 8;
    private static final String VISTA = "admin/usuarios";
    private static final String CORREO_DUPLICADO = "Ya existe un usuario con ese correo";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUsuarioController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String listado(Model model) {
        cargarListado(model);
        return VISTA;
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Usuario usuario = buscar(id);
        model.addAttribute("edicion", true);
        model.addAttribute("id", id);
        model.addAttribute("nombre", usuario.getNombre());
        model.addAttribute("correo", usuario.getCorreo());
        model.addAttribute("rol", usuario.getRol());
        cargarListado(model);
        return VISTA;
    }

    @PostMapping("/crear")
    @Transactional
    public String crear(@RequestParam String nombre,
                        @RequestParam String correo,
                        @RequestParam String password,
                        @RequestParam Rol rol,
                        Model model,
                        RedirectAttributes redirect) {
        String correoNormalizado = normalizarCorreo(correo);
        String error = validarDatos(nombre, correoNormalizado, rol);
        if (error == null && (password == null || password.length() < LONGITUD_MINIMA_PASSWORD)) {
            error = "La contraseña debe tener mínimo 8 caracteres";
        }
        if (error == null && usuarioRepository.findByCorreo(correoNormalizado).isPresent()) {
            error = CORREO_DUPLICADO;
        }
        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("nombre", nombre);
            model.addAttribute("correo", correoNormalizado);
            model.addAttribute("rol", rol);
            cargarListado(model);
            return VISTA;
        }
        usuarioRepository.save(new Usuario(nombre.trim(), correoNormalizado,
                passwordEncoder.encode(password), rol));
        redirect.addFlashAttribute("mensaje", "Usuario creado exitosamente");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/{id}/editar")
    @Transactional
    public String actualizar(@PathVariable Long id,
                             @RequestParam String nombre,
                             @RequestParam String correo,
                             @RequestParam Rol rol,
                             Authentication authentication,
                             Model model,
                             RedirectAttributes redirect) {
        Usuario usuario = buscar(id);
        String correoNormalizado = normalizarCorreo(correo);
        String error = validarDatos(nombre, correoNormalizado, rol);
        if (error == null && esPropiaCuenta(usuario, authentication) && rol != Rol.ADMIN) {
            error = "No puede quitarse a sí mismo el rol de administrador";
        }
        if (error == null && usuarioRepository.findByCorreo(correoNormalizado)
                .filter(otro -> !otro.getId().equals(id)).isPresent()) {
            error = CORREO_DUPLICADO;
        }
        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("edicion", true);
            model.addAttribute("id", id);
            model.addAttribute("nombre", nombre);
            model.addAttribute("correo", correoNormalizado);
            model.addAttribute("rol", rol);
            cargarListado(model);
            return VISTA;
        }
        usuario.actualizarDatos(nombre.trim(), correoNormalizado, rol);
        usuarioRepository.save(usuario);
        redirect.addFlashAttribute("mensaje", "Usuario actualizado exitosamente");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/{id}/toggle-activo")
    @Transactional
    public String cambiarEstado(@PathVariable Long id,
                                Authentication authentication,
                                RedirectAttributes redirect) {
        Usuario usuario = buscar(id);
        if (usuario.isActivo() && esPropiaCuenta(usuario, authentication)) {
            redirect.addFlashAttribute("error", "No puede desactivar su propia cuenta");
            return "redirect:/admin/usuarios";
        }
        if (usuario.isActivo()) {
            usuario.desactivar();
            redirect.addFlashAttribute("mensaje", "Usuario desactivado exitosamente");
        } else {
            usuario.activar();
            redirect.addFlashAttribute("mensaje", "Usuario activado exitosamente");
        }
        usuarioRepository.save(usuario);
        return "redirect:/admin/usuarios";
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private void cargarListado(Model model) {
        model.addAttribute("usuarios", usuarioRepository.findAll(Sort.by("nombre")));
        model.addAttribute("roles", Rol.values());
    }

    private String validarDatos(String nombre, String correo, Rol rol) {
        if (nombre == null || nombre.isBlank()) {
            return "El nombre es obligatorio";
        }
        if (!CORREO.matcher(correo).matches()) {
            return "El correo no es válido";
        }
        if (rol == null) {
            return "Debe asignar un rol";
        }
        return null;
    }

    private String normalizarCorreo(String correo) {
        return correo == null ? null : correo.trim().toLowerCase(Locale.ROOT);
    }

    private boolean esPropiaCuenta(Usuario usuario, Authentication authentication) {
        return authentication != null && usuario.getCorreo().equals(authentication.getName());
    }
}
