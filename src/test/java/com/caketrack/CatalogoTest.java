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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CatalogoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IngredienteRepository ingredienteRepository;

    @Autowired
    private ProductoRepository productoRepository;

    private Ingrediente persistirIngrediente() {
        return ingredienteRepository.save(new Ingrediente("Insumo de catálogo", "kg",
                new BigDecimal("10.00"), new BigDecimal("2.00")));
    }

    private Producto persistirProducto(String nombre, String descripcion, TipoProducto tipo,
                                       String precio, Ingrediente ingrediente, boolean disponible) {
        Producto producto = new Producto(nombre, tipo, new BigDecimal(precio), descripcion);
        producto.agregarIngrediente(ingrediente, new BigDecimal("0.5"));
        if (disponible) {
            producto.marcarDisponible();
        }
        return productoRepository.save(producto);
    }

    @Test
    void clienteVeSoloProductosDisponiblesConNombreDescripcionYPrecio() throws Exception {
        Ingrediente ingrediente = persistirIngrediente();
        persistirProducto("Torta Visible de Prueba", "Descripción visible", TipoProducto.TORTA,
                "80000.00", ingrediente, true);
        persistirProducto("Torta Oculta de Prueba", "Descripción oculta", TipoProducto.TORTA,
                "90000.00", ingrediente, false);

        mockMvc.perform(get("/catalogo").with(user("cliente-prueba@caketrack.com").roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Torta Visible de Prueba")))
                .andExpect(content().string(containsString("Descripción visible")))
                .andExpect(content().string(containsString("80.000")))
                .andExpect(content().string(not(containsString("Torta Oculta de Prueba"))));
    }

    @Test
    void cajeroPuedeConsultarElCatalogo() throws Exception {
        mockMvc.perform(get("/catalogo").with(user("cajero-prueba@caketrack.com").roles("CAJERO")))
                .andExpect(status().isOk());
    }

    @Test
    void filtroPorTipoMuestraSoloEseTipo() throws Exception {
        Ingrediente ingrediente = persistirIngrediente();
        persistirProducto("Torta Filtro de Prueba", "Torta de prueba", TipoProducto.TORTA,
                "80000.00", ingrediente, true);
        persistirProducto("Postre Filtro de Prueba", "Postre de prueba", TipoProducto.POSTRE,
                "12000.00", ingrediente, true);

        mockMvc.perform(get("/catalogo").param("tipo", "POSTRE")
                        .with(user("cliente-prueba@caketrack.com").roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Postre Filtro de Prueba")))
                .andExpect(content().string(not(containsString("Torta Filtro de Prueba"))));
    }

    @Test
    void catalogoVacioMuestraMensaje() throws Exception {
        productoRepository.deleteAll();

        mockMvc.perform(get("/catalogo").with(user("cliente-prueba@caketrack.com").roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No hay productos disponibles en este momento")));
    }
}
