package com.caketrack;

import com.caketrack.model.EstadoPedido;
import com.caketrack.model.Ingrediente;
import com.caketrack.model.Pedido;
import com.caketrack.model.Producto;
import com.caketrack.model.Rol;
import com.caketrack.model.TipoProducto;
import com.caketrack.model.Usuario;
import com.caketrack.repository.IngredienteRepository;
import com.caketrack.repository.PedidoRepository;
import com.caketrack.repository.ProductoRepository;
import com.caketrack.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.containsString;
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
class PedidoAbonoTest {

    private static final String CORREO_CLIENTE = "cliente-pedido@caketrack.com";
    private static final String CORREO_CAJERO = "cajero-pedido@caketrack.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private IngredienteRepository ingredienteRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    private Usuario persistirCliente() {
        return usuarioRepository.save(new Usuario("Cliente Pedido", CORREO_CLIENTE, "no-usado", Rol.CLIENTE));
    }

    private Producto persistirProductoDisponible(String nombre, String precio) {
        Ingrediente ingrediente = ingredienteRepository.save(
                new Ingrediente("Insumo " + nombre, "kg", new BigDecimal("50"), new BigDecimal("5")));
        Producto producto = new Producto(nombre, TipoProducto.TORTA, new BigDecimal(precio), null);
        producto.agregarIngrediente(ingrediente, new BigDecimal("0.5"));
        producto.marcarDisponible();
        return productoRepository.save(producto);
    }

    private Pedido persistirPersonalizadoConTotal(Usuario cliente, String total) {
        Pedido pedido = new Pedido(cliente, LocalDateTime.now(), LocalDateTime.now().plusHours(72), true);
        pedido.fijarTotal(new BigDecimal(total));
        pedidoRepository.saveAndFlush(pedido);
        pedido.asignarCodigo();
        return pedido;
    }

    private Pedido persistirPersonalizadoSinTotal(Usuario cliente) {
        Pedido pedido = new Pedido(cliente, LocalDateTime.now(), LocalDateTime.now().plusHours(72), true);
        pedidoRepository.saveAndFlush(pedido);
        pedido.asignarCodigo();
        return pedido;
    }

    private String fechaEntregaDentroDe(long horas) {
        return LocalDateTime.now().plusHours(horas).truncatedTo(ChronoUnit.MINUTES).toString();
    }

    private Pedido pedidoConSabor(String sabor) {
        return pedidoRepository.findAll().stream()
                .filter(pedido -> sabor.equals(pedido.getSaborBizcocho()))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void pedidoPersonalizadoConAnticipacionValidaSeCreaPendienteDeAbono() throws Exception {
        persistirCliente();

        mockMvc.perform(post("/pedidos/nuevo").with(csrf()).with(user(CORREO_CLIENTE).roles("CLIENTE"))
                        .param("esPersonalizado", "true")
                        .param("saborBizcocho", "Sabor prueba AC-008.1")
                        .param("relleno", "Crema de maracuyá")
                        .param("porciones", "12")
                        .param("dedicatoria", "Feliz cumpleaños Ana")
                        .param("fechaEntrega", fechaEntregaDentroDe(48)))
                .andExpect(redirectedUrl("/pedidos/nuevo"));

        Pedido creado = pedidoConSabor("Sabor prueba AC-008.1");
        assertEquals(EstadoPedido.PENDIENTE_ABONO, creado.getEstado());
        assertTrue(creado.getCodigoPedido().startsWith("CT-" + LocalDateTime.now().getYear() + "-"));
    }

    @Test
    void pedidoPersonalizadoConMenosDe24HorasSeRechaza() throws Exception {
        persistirCliente();
        long antes = pedidoRepository.count();

        mockMvc.perform(post("/pedidos/nuevo").with(csrf()).with(user(CORREO_CLIENTE).roles("CLIENTE"))
                        .param("esPersonalizado", "true")
                        .param("saborBizcocho", "Sabor corto")
                        .param("relleno", "Crema")
                        .param("porciones", "8")
                        .param("fechaEntrega", fechaEntregaDentroDe(10)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("La entrega debe programarse con al menos 24 horas de anticipación")));

        assertEquals(antes, pedidoRepository.count());
    }

    @Test
    void pedidoDeCatalogoCalculaSubtotalSegunCantidad() throws Exception {
        Usuario cliente = persistirCliente();
        Producto torta = persistirProductoDisponible("Torta catálogo AC-008.3", "45000");

        mockMvc.perform(post("/pedidos/nuevo").with(csrf()).with(user(CORREO_CLIENTE).roles("CLIENTE"))
                        .param("productoId", torta.getId().toString())
                        .param("cantidad", "2")
                        .param("fechaEntrega", fechaEntregaDentroDe(48)))
                .andExpect(redirectedUrl("/pedidos/nuevo"));

        Pedido pedido = pedidoRepository.findByClienteOrderByFechaEntregaAsc(cliente).get(0);
        assertEquals(0, new BigDecimal("90000").compareTo(pedido.getDetalles().get(0).getSubtotal()));
    }

    @Test
    void cajeroFijaTotalDePersonalizadoSinDetalles() throws Exception {
        Pedido pedido = persistirPersonalizadoSinTotal(persistirCliente());

        mockMvc.perform(post("/caja/pedidos/{id}/total", pedido.getId()).with(csrf()).with(user(CORREO_CAJERO).roles("CAJERO"))
                        .param("total", "150000"))
                .andExpect(redirectedUrl("/caja/validacion-abonos"));

        assertEquals(0, new BigDecimal("150000").compareTo(pedidoRepository.findById(pedido.getId()).orElseThrow().getTotal()));
    }

    @Test
    void totalCeroEnPersonalizadoSeRechaza() throws Exception {
        Pedido pedido = persistirPersonalizadoSinTotal(persistirCliente());

        mockMvc.perform(post("/caja/pedidos/{id}/total", pedido.getId()).with(csrf()).with(user(CORREO_CAJERO).roles("CAJERO"))
                        .param("total", "0"))
                .andExpect(redirectedUrl("/caja/validacion-abonos"))
                .andExpect(flash().attribute("error", "El total del pedido debe ser mayor que cero"));

        assertEquals(0, BigDecimal.ZERO.compareTo(pedidoRepository.findById(pedido.getId()).orElseThrow().getTotal()));
    }

    @Test
    void totalDePedidoDeCatalogoEsLaSumaDeSubtotales() throws Exception {
        Usuario cliente = persistirCliente();
        Producto torta = persistirProductoDisponible("Torta suma AC-009.1", "45000");
        Producto postre = persistirProductoDisponible("Postre suma AC-009.1", "15000");

        mockMvc.perform(post("/pedidos/nuevo").with(csrf()).with(user(CORREO_CLIENTE).roles("CLIENTE"))
                        .param("productoId", torta.getId().toString(), postre.getId().toString())
                        .param("cantidad", "1", "2")
                        .param("fechaEntrega", fechaEntregaDentroDe(48)))
                .andExpect(redirectedUrl("/pedidos/nuevo"));

        Pedido pedido = pedidoRepository.findByClienteOrderByFechaEntregaAsc(cliente).get(0);
        assertEquals(0, new BigDecimal("75000").compareTo(pedido.getTotal()));
    }

    @Test
    void cajeroRegistraAbonoConReferenciaDePago() throws Exception {
        Pedido pedido = persistirPersonalizadoConTotal(persistirCliente(), "75000");

        mockMvc.perform(post("/caja/pedidos/{id}/abono", pedido.getId()).with(csrf()).with(user(CORREO_CAJERO).roles("CAJERO"))
                        .param("monto", "40000")
                        .param("referenciaPago", "NEQUI-8821"))
                .andExpect(redirectedUrl("/caja/validacion-abonos"));

        Pedido actualizado = pedidoRepository.findById(pedido.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("40000").compareTo(actualizado.getMontoAbonado()));
        assertEquals("NEQUI-8821", actualizado.getReferenciaPago());
        assertEquals(EstadoPedido.PENDIENTE_ABONO, actualizado.getEstado());
    }

    @Test
    void abonoInferiorAl50PorcientoNoPasaACola() throws Exception {
        Pedido pedido = persistirPersonalizadoConTotal(persistirCliente(), "75000");
        pedido.registrarPago(new BigDecimal("30000"), "EFECTIVO-1");

        mockMvc.perform(post("/caja/pedidos/{id}/validar-abono", pedido.getId()).with(csrf()).with(user(CORREO_CAJERO).roles("CAJERO")))
                .andExpect(redirectedUrl("/caja/validacion-abonos"))
                .andExpect(flash().attribute("error", "El abono mínimo es el 50 % del total (37500)"));

        Pedido actualizado = pedidoRepository.findById(pedido.getId()).orElseThrow();
        assertEquals(EstadoPedido.PENDIENTE_ABONO, actualizado.getEstado());
        assertEquals(false, actualizado.isAbonoValidado());
    }

    @Test
    void abonoMayorAl50PorcientoPasaAEnCola() throws Exception {
        Pedido pedido = persistirPersonalizadoConTotal(persistirCliente(), "75000");
        pedido.registrarPago(new BigDecimal("40000"), "NEQUI-8822");

        mockMvc.perform(post("/caja/pedidos/{id}/validar-abono", pedido.getId()).with(csrf()).with(user(CORREO_CAJERO).roles("CAJERO")))
                .andExpect(redirectedUrl("/caja/validacion-abonos"));

        Pedido actualizado = pedidoRepository.findById(pedido.getId()).orElseThrow();
        assertEquals(EstadoPedido.EN_COLA, actualizado.getEstado());
        assertTrue(actualizado.isAbonoValidado());
    }

    @Test
    void abonoExactamenteAl50PorcientoPasaAEnCola() throws Exception {
        Pedido pedido = persistirPersonalizadoConTotal(persistirCliente(), "75000");
        pedido.registrarPago(new BigDecimal("37500"), "NEQUI-8823");

        mockMvc.perform(post("/caja/pedidos/{id}/validar-abono", pedido.getId()).with(csrf()).with(user(CORREO_CAJERO).roles("CAJERO")))
                .andExpect(redirectedUrl("/caja/validacion-abonos"));

        assertEquals(EstadoPedido.EN_COLA, pedidoRepository.findById(pedido.getId()).orElseThrow().getEstado());
    }
}
