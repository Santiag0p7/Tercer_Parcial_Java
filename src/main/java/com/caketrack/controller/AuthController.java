package com.caketrack.controller;

import com.caketrack.model.Usuario;
import com.caketrack.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Controller
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private static final int LONGITUD_MINIMA_PASSWORD = 8;
    private static final String MENSAJE_TOKEN_NO_VALIDO =
            "El enlace de recuperación ha expirado o ya fue utilizado. Solicite uno nuevo";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String raiz() {
        return "redirect:/inicio";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/inicio")
    public String inicio() {
        return "inicio";
    }

    @GetMapping("/recuperar")
    public String recuperar() {
        return "recuperar";
    }

    @PostMapping("/recuperar")
    @Transactional
    public String solicitarRecuperacion(@RequestParam String correo) {
        usuarioRepository.findByCorreo(correo)
                .filter(Usuario::isActivo)
                .ifPresent(usuario -> {
                    String token = UUID.randomUUID().toString();
                    usuario.generarTokenRecuperacion(token, LocalDateTime.now());
                    usuarioRepository.save(usuario);
                    log.info("Enlace de recuperación para {}: /restablecer?token={}", correo, token);
                });
        return "redirect:/recuperar?enviado";
    }

    @GetMapping("/restablecer")
    public String formularioRestablecer(@RequestParam String token, Model model) {
        boolean valido = usuarioRepository.findByTokenRecuperacion(token)
                .filter(usuario -> usuario.tokenValido(LocalDateTime.now()))
                .isPresent();
        return vistaRestablecer(model, token, valido, valido ? null : MENSAJE_TOKEN_NO_VALIDO);
    }

    @PostMapping("/restablecer")
    @Transactional
    public String restablecer(@RequestParam String token,
                              @RequestParam String password,
                              @RequestParam String confirmacion,
                              Model model) {
        Optional<Usuario> usuario = usuarioRepository.findByTokenRecuperacion(token)
                .filter(candidato -> candidato.tokenValido(LocalDateTime.now()));
        if (usuario.isEmpty()) {
            return vistaRestablecer(model, token, false, MENSAJE_TOKEN_NO_VALIDO);
        }
        if (password.length() < LONGITUD_MINIMA_PASSWORD) {
            return vistaRestablecer(model, token, true, "La contraseña debe tener mínimo 8 caracteres");
        }
        if (!password.equals(confirmacion)) {
            return vistaRestablecer(model, token, true, "Las contraseñas no coinciden");
        }
        usuario.get().cambiarPassword(passwordEncoder.encode(password));
        usuarioRepository.save(usuario.get());
        return "redirect:/login?restablecido";
    }

    private String vistaRestablecer(Model model, String token, boolean valido, String error) {
        model.addAttribute("token", token);
        model.addAttribute("valido", valido);
        model.addAttribute("error", error);
        return "restablecer";
    }
}
