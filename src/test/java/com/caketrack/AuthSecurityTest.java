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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthSecurityTest {

    private static final String PASSWORD = "Password123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private void crearUsuario(String correo, Rol rol, boolean activo) {
        Usuario usuario = new Usuario("Usuario de prueba", correo, passwordEncoder.encode(PASSWORD), rol);
        if (!activo) {
            usuario.desactivar();
        }
        usuarioRepository.save(usuario);
    }

    @Test
    void rutasPublicasSonAccesiblesSinSesion() throws Exception {
        mockMvc.perform(get("/login")).andExpect(status().isOk());
        mockMvc.perform(get("/recuperar")).andExpect(status().isOk());
    }

    @Test
    void loginConCredencialesValidasYUsuarioActivoRedirigeAlInicio() throws Exception {
        crearUsuario("activo@caketrack.com", Rol.CLIENTE, true);

        mockMvc.perform(post("/login").with(csrf())
                        .param("username", "activo@caketrack.com")
                        .param("password", PASSWORD))
                .andExpect(redirectedUrl("/inicio"));
    }

    @Test
    void loginConPasswordIncorrectaEsRechazado() throws Exception {
        crearUsuario("activo@caketrack.com", Rol.CLIENTE, true);

        mockMvc.perform(post("/login").with(csrf())
                        .param("username", "activo@caketrack.com")
                        .param("password", "PasswordIncorrecto"))
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void loginDeUsuarioInactivoEsRechazadoAunqueLaPasswordSeaValida() throws Exception {
        crearUsuario("inactivo@caketrack.com", Rol.CLIENTE, false);

        mockMvc.perform(post("/login").with(csrf())
                        .param("username", "inactivo@caketrack.com")
                        .param("password", PASSWORD))
                .andExpect(redirectedUrl("/login?inactivo"));
    }

    @Test
    void logoutCierraSesionYRedirigeAlLogin() throws Exception {
        mockMvc.perform(post("/logout").with(csrf())
                        .with(user("activo@caketrack.com").roles("CLIENTE")))
                .andExpect(redirectedUrl("/login?logout"));
    }

    @Test
    void recuperacionGeneraTokenConVigenciaDeTreintaMinutos() throws Exception {
        crearUsuario("recuperar@caketrack.com", Rol.CLIENTE, true);
        LocalDateTime antes = LocalDateTime.now();

        mockMvc.perform(post("/recuperar").with(csrf())
                        .param("correo", "recuperar@caketrack.com"))
                .andExpect(redirectedUrl("/recuperar?enviado"));

        LocalDateTime despues = LocalDateTime.now();
        Usuario usuario = usuarioRepository.findByCorreo("recuperar@caketrack.com").orElseThrow();
        assertNotNull(usuario.getTokenRecuperacion());
        assertFalse(usuario.isTokenUsado());
        assertFalse(usuario.getTokenExpiracion().isBefore(antes.plus(Usuario.VIGENCIA_TOKEN)));
        assertFalse(usuario.getTokenExpiracion().isAfter(despues.plus(Usuario.VIGENCIA_TOKEN)));
    }

    @Test
    void recuperacionConCorreoInexistenteDaRespuestaGenerica() throws Exception {
        mockMvc.perform(post("/recuperar").with(csrf())
                        .param("correo", "noexiste@caketrack.com"))
                .andExpect(redirectedUrl("/recuperar?enviado"));

        assertTrue(usuarioRepository.findByCorreo("noexiste@caketrack.com").isEmpty());
    }

    @Test
    void tokenExpiraExactamenteATreintaMinutos() {
        Usuario usuario = new Usuario("Usuario de prueba", "token@caketrack.com", "hash", Rol.CLIENTE);
        LocalDateTime generacion = LocalDateTime.of(2026, 11, 17, 10, 0);
        usuario.generarTokenRecuperacion("token", generacion);

        assertTrue(usuario.tokenValido(generacion.plusMinutes(29).plusSeconds(59)));
        assertTrue(usuario.tokenExpirado(generacion.plusMinutes(30)));
    }

    @Test
    void clienteRecibe403EnRutasAdmin() throws Exception {
        mockMvc.perform(get("/admin/usuarios").with(user("cliente@caketrack.com").roles("CLIENTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void pasteleroRecibe403EnGestionDeUsuarios() throws Exception {
        mockMvc.perform(get("/admin/usuarios").with(user("pastelero@caketrack.com").roles("PASTELERO")))
                .andExpect(status().isForbidden());
    }

    @Test
    void pasteleroAccedeAIngredientesYClienteRecibe403() throws Exception {
        int estadoPastelero = mockMvc.perform(get("/admin/ingredientes")
                        .with(user("pastelero@caketrack.com").roles("PASTELERO")))
                .andReturn().getResponse().getStatus();
        assertNotEquals(403, estadoPastelero);

        mockMvc.perform(get("/admin/ingredientes").with(user("cliente@caketrack.com").roles("CLIENTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminNoRecibe403EnRutasAdmin() throws Exception {
        int estado = mockMvc.perform(get("/admin/usuarios").with(user("admin@caketrack.com").roles("ADMIN")))
                .andReturn().getResponse().getStatus();
        assertNotEquals(403, estado);
    }

    @Test
    void anonimoEsRedirigidoAlLoginEnRutasProtegidas() throws Exception {
        mockMvc.perform(get("/admin/usuarios"))
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
