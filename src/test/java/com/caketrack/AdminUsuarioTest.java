package com.caketrack;

import com.caketrack.model.Rol;
import com.caketrack.model.Usuario;
import com.caketrack.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminUsuarioTest {

    private static final String PASSWORD = "Password123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static RequestPostProcessor administrador() {
        return user("admin-prueba@caketrack.com").roles("ADMIN");
    }

    private Usuario persistir(String correo, Rol rol) {
        return usuarioRepository.save(new Usuario("Usuario de prueba", correo, passwordEncoder.encode(PASSWORD), rol));
    }

    @Test
    void adminCreaEmpleadoConCadaRolYCifraLaClave() throws Exception {
        for (Rol rol : Rol.values()) {
            String correo = "nuevo-" + rol.name().toLowerCase() + "@caketrack.com";

            mockMvc.perform(post("/admin/usuarios/crear").with(csrf()).with(administrador())
                            .param("nombre", "Empleado " + rol.name())
                            .param("correo", correo)
                            .param("password", PASSWORD)
                            .param("rol", rol.name()))
                    .andExpect(redirectedUrl("/admin/usuarios"));

            Usuario creado = usuarioRepository.findByCorreo(correo).orElseThrow();
            assertEquals(rol, creado.getRol());
            assertTrue(creado.isActivo());
            assertNotEquals(PASSWORD, creado.getPassword());
            assertTrue(passwordEncoder.matches(PASSWORD, creado.getPassword()));
        }
    }

    @Test
    void adminEditaElRolDeUnEmpleado() throws Exception {
        Usuario empleado = persistir("editar@caketrack.com", Rol.CAJERO);

        mockMvc.perform(post("/admin/usuarios/{id}/editar", empleado.getId()).with(csrf()).with(administrador())
                        .param("nombre", "Empleado editado")
                        .param("correo", "editar@caketrack.com")
                        .param("rol", "PASTELERO"))
                .andExpect(redirectedUrl("/admin/usuarios"));

        assertEquals(Rol.PASTELERO, usuarioRepository.findById(empleado.getId()).orElseThrow().getRol());
    }

    @Test
    void adminActivaYDesactivaUnaCuenta() throws Exception {
        Usuario empleado = persistir("estado@caketrack.com", Rol.CLIENTE);

        mockMvc.perform(post("/admin/usuarios/{id}/toggle-activo", empleado.getId()).with(csrf()).with(administrador()))
                .andExpect(redirectedUrl("/admin/usuarios"));
        assertFalse(usuarioRepository.findById(empleado.getId()).orElseThrow().isActivo());

        mockMvc.perform(post("/admin/usuarios/{id}/toggle-activo", empleado.getId()).with(csrf()).with(administrador()))
                .andExpect(redirectedUrl("/admin/usuarios"));
        assertTrue(usuarioRepository.findById(empleado.getId()).orElseThrow().isActivo());
    }

    @Test
    void adminNoPuedeDesactivarSuPropiaCuenta() throws Exception {
        Usuario admin = persistir("admin-prueba@caketrack.com", Rol.ADMIN);

        mockMvc.perform(post("/admin/usuarios/{id}/toggle-activo", admin.getId()).with(csrf())
                        .with(user(admin.getCorreo()).roles("ADMIN")))
                .andExpect(redirectedUrl("/admin/usuarios"))
                .andExpect(flash().attributeExists("error"));

        assertTrue(usuarioRepository.findById(admin.getId()).orElseThrow().isActivo());
    }

    @Test
    void correoDuplicadoMuestraErrorSinRomperLaAplicacion() throws Exception {
        persistir("duplicado@caketrack.com", Rol.CLIENTE);

        mockMvc.perform(post("/admin/usuarios/crear").with(csrf()).with(administrador())
                        .param("nombre", "Otro usuario")
                        .param("correo", "duplicado@caketrack.com")
                        .param("password", PASSWORD)
                        .param("rol", "CAJERO"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ya existe un usuario con ese correo")));

        long cantidad = usuarioRepository.findAll().stream()
                .filter(usuario -> usuario.getCorreo().equals("duplicado@caketrack.com"))
                .count();
        assertEquals(1, cantidad);
    }

    @Test
    void passwordMenorA8CaracteresNoCreaElUsuario() throws Exception {
        mockMvc.perform(post("/admin/usuarios/crear").with(csrf()).with(administrador())
                        .param("nombre", "Usuario corto")
                        .param("correo", "corta@caketrack.com")
                        .param("password", "Pass1")
                        .param("rol", "CAJERO"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("8 caracteres")));

        assertFalse(usuarioRepository.findByCorreo("corta@caketrack.com").isPresent());
    }
}
