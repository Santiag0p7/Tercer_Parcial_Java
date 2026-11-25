package com.caketrack.controller;

import com.caketrack.model.Pedido;
import com.caketrack.model.Producto;
import com.caketrack.model.Rol;
import com.caketrack.model.Usuario;
import com.caketrack.repository.PedidoRepository;
import com.caketrack.repository.ProductoRepository;
import com.caketrack.repository.UsuarioRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class PedidoController {

    private static final String VISTA_NUEVO = "pedidos/nuevo";
    private static final String REDIRECT_NUEVO = "redirect:/pedidos/nuevo";
    private static final String MENSAJE_ANTICIPACION = "La entrega debe programarse con al menos 24 horas de anticipación";
    private static final String MENSAJE_PERSONALIZADO = "Los pedidos personalizados requieren sabor, relleno y porciones";

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public PedidoController(PedidoRepository pedidoRepository, ProductoRepository productoRepository,
                            UsuarioRepository usuarioRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/pedidos/nuevo")
    public String formulario(Model model) {
        cargarFormulario(model);
        return VISTA_NUEVO;
    }

    @PostMapping("/pedidos/nuevo")
    @Transactional
    public String crearPedido(Principal principal,
                              @RequestParam(name = "clienteId", required = false) Long clienteId,
                              @RequestParam(name = "esPersonalizado", defaultValue = "false") boolean esPersonalizado,
                              @RequestParam(name = "saborBizcocho", required = false) String saborBizcocho,
                              @RequestParam(name = "relleno", required = false) String relleno,
                              @RequestParam(name = "cobertura", required = false) String cobertura,
                              @RequestParam(name = "dedicatoria", required = false) String dedicatoria,
                              @RequestParam(name = "porciones", required = false) Integer porciones,
                              @RequestParam(name = "notasAdicionales", required = false) String notasAdicionales,
                              @RequestParam(name = "fechaEntrega")
                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaEntrega,
                              @RequestParam(name = "productoId", required = false) List<Long> productoIds,
                              @RequestParam(name = "cantidad", required = false) List<Integer> cantidades,
                              Model model,
                              RedirectAttributes redirect) {
        LocalDateTime ahora = LocalDateTime.now();
        Usuario usuario = usuarioActual(principal);
        Usuario cliente = usuario.getRol() == Rol.CLIENTE ? usuario : buscarCliente(clienteId);

        String error = null;
        if (cliente == null) {
            error = "Seleccione el cliente del pedido";
        }

        Pedido pedido = null;
        if (error == null) {
            pedido = new Pedido(cliente, ahora, fechaEntrega, esPersonalizado);
            error = agregarProductos(pedido, productoIds, cantidades);
        }
        if (error == null && esPersonalizado) {
            if (!tieneEspecificacionesMinimas(saborBizcocho, relleno, porciones)) {
                error = MENSAJE_PERSONALIZADO;
            } else if (!pedido.cumpleAnticipacion(ahora)) {
                error = MENSAJE_ANTICIPACION;
            }
        }
        if (error == null && !esPersonalizado && pedido.getDetalles().isEmpty()) {
            error = "Seleccione al menos un producto del catálogo o un pedido personalizado";
        }
        if (error == null && !esPersonalizado && fechaEntrega.isBefore(ahora)) {
            error = "La fecha de entrega debe ser futura";
        }

        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("clienteId", clienteId);
            model.addAttribute("esPersonalizado", esPersonalizado);
            model.addAttribute("saborBizcocho", saborBizcocho);
            model.addAttribute("relleno", relleno);
            model.addAttribute("cobertura", cobertura);
            model.addAttribute("dedicatoria", dedicatoria);
            model.addAttribute("porciones", porciones);
            model.addAttribute("notasAdicionales", notasAdicionales);
            cargarFormulario(model);
            return VISTA_NUEVO;
        }

        pedido.definirPersonalizacion(saborBizcocho, relleno, cobertura, dedicatoria, porciones, notasAdicionales);
        pedidoRepository.saveAndFlush(pedido);
        pedido.asignarCodigo();
        redirect.addFlashAttribute("mensaje", "Pedido " + pedido.getCodigoPedido() + " registrado exitosamente");
        return REDIRECT_NUEVO;
    }

    private String agregarProductos(Pedido pedido, List<Long> productoIds, List<Integer> cantidades) {
        if (productoIds == null) {
            return null;
        }
        for (int i = 0; i < productoIds.size(); i++) {
            Long productoId = productoIds.get(i);
            Integer cantidad = cantidades != null && i < cantidades.size() ? cantidades.get(i) : null;
            if (productoId == null || cantidad == null || cantidad == 0) {
                continue;
            }
            if (cantidad < 0) {
                return "La cantidad de cada producto debe ser mayor que cero";
            }
            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
            if (!producto.isDisponibleReal()) {
                return "El producto " + producto.getNombre() + " no está disponible";
            }
            pedido.agregarProducto(producto, cantidad);
        }
        return null;
    }

    private boolean tieneEspecificacionesMinimas(String sabor, String relleno, Integer porciones) {
        return sabor != null && !sabor.isBlank()
                && relleno != null && !relleno.isBlank()
                && porciones != null && porciones > 0;
    }

    private Usuario usuarioActual(Principal principal) {
        return usuarioRepository.findByCorreo(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no autorizado"));
    }

    private Usuario buscarCliente(Long clienteId) {
        if (clienteId == null) {
            return null;
        }
        return usuarioRepository.findById(clienteId)
                .filter(candidato -> candidato.getRol() == Rol.CLIENTE)
                .orElse(null);
    }

    private void cargarFormulario(Model model) {
        model.addAttribute("productos", productoRepository.findByDisponibleTrue().stream()
                .filter(Producto::isDisponibleReal)
                .toList());
        model.addAttribute("clientes", usuarioRepository.findAll().stream()
                .filter(usuario -> usuario.getRol() == Rol.CLIENTE)
                .toList());
    }
}
