package com.caketrack;

import com.caketrack.model.Ingrediente;
import com.caketrack.model.Producto;
import com.caketrack.model.TipoProducto;
import com.caketrack.repository.IngredienteRepository;
import com.caketrack.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LandingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IngredienteRepository ingredienteRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Test
    void landingEsPublicaYTieneElFormularioDeIngreso() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tortas y postres hechos a mano")))
                .andExpect(content().string(containsString("action=\"/login\"")));
    }

    @Test
    void landingMuestraLosProductosDisponibles() throws Exception {
        Ingrediente ingrediente = ingredienteRepository.save(
                new Ingrediente("Insumo landing", "kg", new BigDecimal("5.00"), new BigDecimal("1.00")));
        Producto producto = new Producto("Torta Landing de Prueba", TipoProducto.TORTA,
                new BigDecimal("70000.00"), "Descripción landing");
        producto.agregarIngrediente(ingrediente, new BigDecimal("0.5"));
        producto.marcarDisponible();
        productoRepository.save(producto);

        mockMvc.perform(get("/"))
                .andExpect(content().string(containsString("Torta Landing de Prueba")));
    }

    @Test
    void usuarioAutenticadoEnLaRaizVaAlDashboard() throws Exception {
        mockMvc.perform(get("/").with(user("cliente-prueba@caketrack.com").roles("CLIENTE")))
                .andExpect(redirectedUrl("/inicio"));
    }

    @Test
    void dashboardRequiereSesion() throws Exception {
        mockMvc.perform(get("/inicio"))
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void dashboardMuestraSoloLasOpcionesDelRol() throws Exception {
        mockMvc.perform(get("/inicio").with(user("cliente-prueba@caketrack.com").roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Administrar usuarios"))));

        mockMvc.perform(get("/inicio").with(user("admin-prueba@caketrack.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Administrar usuarios")));
    }
}
