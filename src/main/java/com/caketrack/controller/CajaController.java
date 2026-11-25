package com.caketrack.controller;

import com.caketrack.model.EstadoPedido;
import com.caketrack.model.Pedido;
import com.caketrack.repository.PedidoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
public class CajaController {

    private static final String VISTA_VALIDACION = "caja/validacion-abonos";
    private static final String REDIRECT_VALIDACION = "redirect:/caja/validacion-abonos";

    private final PedidoRepository pedidoRepository;

    public CajaController(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @GetMapping("/caja/validacion-abonos")
    public String listado(Model model) {
        model.addAttribute("pedidos", pedidoRepository.findByEstadoOrderByFechaCreacionAsc(EstadoPedido.PENDIENTE_ABONO));
        return VISTA_VALIDACION;
    }

    @PostMapping("/caja/pedidos/{id}/total")
    @Transactional
    public String fijarTotal(@PathVariable Long id,
                             @RequestParam(name = "total", required = false) BigDecimal total,
                             RedirectAttributes redirect) {
        Pedido pedido = buscarPedido(id);
        try {
            pedido.fijarTotal(total);
            redirect.addFlashAttribute("mensaje", "Total del pedido " + pedido.getCodigoPedido() + " registrado");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return REDIRECT_VALIDACION;
    }

    @PostMapping("/caja/pedidos/{id}/abono")
    @Transactional
    public String registrarAbono(@PathVariable Long id,
                                 @RequestParam(name = "monto", required = false) BigDecimal monto,
                                 @RequestParam(name = "referenciaPago", required = false) String referenciaPago,
                                 RedirectAttributes redirect) {
        Pedido pedido = buscarPedido(id);
        if (referenciaPago == null || referenciaPago.isBlank()) {
            redirect.addFlashAttribute("error", "La referencia de pago es obligatoria");
            return REDIRECT_VALIDACION;
        }
        try {
            pedido.registrarPago(monto, referenciaPago.trim());
            redirect.addFlashAttribute("mensaje", "Abono registrado para el pedido " + pedido.getCodigoPedido());
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return REDIRECT_VALIDACION;
    }

    @PostMapping("/caja/pedidos/{id}/validar-abono")
    @Transactional
    public String validarAbono(@PathVariable Long id, RedirectAttributes redirect) {
        Pedido pedido = buscarPedido(id);
        try {
            pedido.validarAbono();
            redirect.addFlashAttribute("mensaje", "Abono validado. El pedido " + pedido.getCodigoPedido() + " pasó a cocina");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return REDIRECT_VALIDACION;
    }

    private Pedido buscarPedido(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
    }
}
