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
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InventarioRecetaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IngredienteRepository ingredienteRepository;

    @Autowired
    private ProductoRepository productoRepository;

    private static RequestPostProcessor pastelero() {
        return user("pastelero-prueba@caketrack.com").roles("PASTELERO");
    }

    private Ingrediente persistirIngrediente(String nombre, String cantidad, String minimo) {
        return ingredienteRepository.save(new Ingrediente(nombre, "kg", new BigDecimal(cantidad), new BigDecimal(minimo)));
    }

    private Producto persistirProducto(String nombre) {
        return productoRepository.save(new Producto(nombre, TipoProducto.TORTA, new BigDecimal("50000.00"), null));
    }

    @Test
    void ingredienteConStockIgualAlMinimoActivaAlerta() throws Exception {
        mockMvc.perform(post("/admin/ingredientes/crear").with(csrf()).with(pastelero())
                        .param("nombre", "Insumo alerta")
                        .param("unidadMedida", "kg")
                        .param("cantidadActual", "10")
                        .param("stockMinimo", "10"))
                .andExpect(redirectedUrl("/admin/ingredientes"));

        Ingrediente creado = ingredienteRepository.findByNombre("Insumo alerta").orElseThrow();
        assertTrue(creado.isBajoStockMinimo());
    }

    @Test
    void ingredienteConStockSuperiorAlMinimoNoActivaAlerta() throws Exception {
        mockMvc.perform(post("/admin/ingredientes/crear").with(csrf()).with(pastelero())
                        .param("nombre", "Insumo normal")
                        .param("unidadMedida", "kg")
                        .param("cantidadActual", "50")
                        .param("stockMinimo", "10"))
                .andExpect(redirectedUrl("/admin/ingredientes"));

        assertFalse(ingredienteRepository.findByNombre("Insumo normal").orElseThrow().isBajoStockMinimo());
    }

    @Test
    void actualizarCantidadPorEncimaDelMinimoQuitaLaAlerta() throws Exception {
        Ingrediente ingrediente = persistirIngrediente("Insumo actualizable", "5", "10");
        assertTrue(ingrediente.isBajoStockMinimo());

        mockMvc.perform(post("/admin/ingredientes/{id}/editar", ingrediente.getId()).with(csrf()).with(pastelero())
                        .param("nombre", "Insumo actualizable")
                        .param("unidadMedida", "kg")
                        .param("cantidadActual", "20")
                        .param("stockMinimo", "10"))
                .andExpect(redirectedUrl("/admin/ingredientes"));

        assertFalse(ingredienteRepository.findById(ingrediente.getId()).orElseThrow().isBajoStockMinimo());
    }

    @Test
    void listadoDeIngredientesMuestraLaAlertaDeReposicion() throws Exception {
        persistirIngrediente("Insumo en listado", "5", "10");

        mockMvc.perform(get("/admin/ingredientes").with(pastelero()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Reponer")));
    }

    @Test
    void productoSinRecetaNoPuedeMarcarseDisponible() throws Exception {
        Producto producto = persistirProducto("Torta sin receta");

        assertThrows(IllegalStateException.class, producto::marcarDisponible);

        mockMvc.perform(post("/admin/productos/{id}/disponibilidad", producto.getId()).with(csrf()).with(pastelero()))
                .andExpect(redirectedUrl("/admin/productos"))
                .andExpect(flash().attributeExists("error"));

        assertFalse(productoRepository.findById(producto.getId()).orElseThrow().isDisponible());
    }

    @Test
    void productoConRecetaPuedeMarcarseDisponible() throws Exception {
        Ingrediente ingrediente = persistirIngrediente("Insumo para receta", "10", "2");
        Producto producto = persistirProducto("Torta con receta");

        mockMvc.perform(post("/admin/productos/{id}/receta", producto.getId()).with(csrf()).with(pastelero())
                        .param("ingredienteId", ingrediente.getId().toString())
                        .param("cantidad", "0.5"))
                .andExpect(redirectedUrl("/admin/productos"));

        mockMvc.perform(post("/admin/productos/{id}/disponibilidad", producto.getId()).with(csrf()).with(pastelero()))
                .andExpect(redirectedUrl("/admin/productos"));

        assertTrue(productoRepository.findById(producto.getId()).orElseThrow().isDisponible());
    }

    @Test
    void quitarElUltimoIngredienteDesmarcaElProducto() throws Exception {
        Ingrediente ingrediente = persistirIngrediente("Insumo quitable", "10", "2");
        Producto producto = persistirProducto("Torta quitable");
        producto.agregarIngrediente(ingrediente, new BigDecimal("0.5"));
        producto.marcarDisponible();
        productoRepository.save(producto);

        mockMvc.perform(post("/admin/productos/{id}/receta/{ing}/quitar", producto.getId(), ingrediente.getId())
                        .with(csrf()).with(pastelero()))
                .andExpect(redirectedUrl("/admin/productos"));

        Producto actualizado = productoRepository.findById(producto.getId()).orElseThrow();
        assertFalse(actualizado.isDisponible());
        assertFalse(actualizado.tieneReceta());
    }

    @Test
    void crearProductoConIngredientesQuedaNoDisponible() throws Exception {
        Ingrediente ingrediente = persistirIngrediente("Insumo de postre", "10", "2");

        mockMvc.perform(post("/admin/productos/crear").with(csrf()).with(pastelero())
                        .param("nombre", "Postre de prueba")
                        .param("tipo", "POSTRE")
                        .param("precio", "15000")
                        .param("descripcion", "Postre de prueba")
                        .param("ingredienteId", ingrediente.getId().toString())
                        .param("cantidad", "0.2"))
                .andExpect(redirectedUrl("/admin/productos"));

        Producto creado = productoRepository.findAll().stream()
                .filter(producto -> producto.getNombre().equals("Postre de prueba"))
                .findFirst()
                .orElseThrow();
        assertFalse(creado.isDisponible());
        assertTrue(creado.tieneReceta());
    }

    @Test
    void crearProductoSinIngredientesMuestraErrorYNoLoCrea() throws Exception {
        mockMvc.perform(post("/admin/productos/crear").with(csrf()).with(pastelero())
                        .param("nombre", "Torta vacía")
                        .param("tipo", "TORTA")
                        .param("precio", "20000"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("al menos un ingrediente asociado")));

        assertFalse(productoRepository.findAll().stream()
                .anyMatch(producto -> producto.getNombre().equals("Torta vacía")));
    }
}
