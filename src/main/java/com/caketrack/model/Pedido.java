package com.caketrack.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pedido {

    private static final BigDecimal PORCENTAJE_ABONO_MINIMO = new BigDecimal("0.50");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_pedido", unique = true, length = 20)
    private String codigoPedido;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_entrega", nullable = false)
    private LocalDateTime fechaEntrega;

    @Column(name = "es_personalizado", nullable = false)
    private boolean esPersonalizado;

    @Column(name = "sabor_bizcocho", length = 100)
    private String saborBizcocho;

    @Column(length = 100)
    private String relleno;

    @Column(length = 100)
    private String cobertura;

    @Column(length = 200)
    private String dedicatoria;

    private Integer porciones;

    @Column(name = "notas_adicionales", columnDefinition = "TEXT")
    private String notasAdicionales;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "monto_abonado", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoAbonado = BigDecimal.ZERO;

    @Column(name = "abono_validado", nullable = false)
    private boolean abonoValidado = false;

    @Column(name = "referencia_pago", length = 100)
    private String referenciaPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private EstadoPedido estado = EstadoPedido.PENDIENTE_ABONO;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Pedido(Usuario cliente, LocalDateTime fechaCreacion, LocalDateTime fechaEntrega, boolean esPersonalizado) {
        this.cliente = cliente;
        this.fechaCreacion = fechaCreacion;
        this.fechaEntrega = fechaEntrega;
        this.esPersonalizado = esPersonalizado;
    }

    public void agregarProducto(Producto producto, int cantidad) {
        detalles.add(new DetallePedido(this, producto, cantidad));
        recalcularTotal();
    }

    public void recalcularTotal() {
        this.total = detalles.stream()
                .map(DetallePedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void fijarTotal(BigDecimal nuevoTotal) {
        if (nuevoTotal == null || nuevoTotal.signum() <= 0) {
            throw new IllegalArgumentException("El total del pedido debe ser mayor que cero");
        }
        if (!esPersonalizado || !detalles.isEmpty()) {
            throw new IllegalStateException("Solo los pedidos personalizados sin productos pueden fijar el total manualmente");
        }
        if (estado != EstadoPedido.PENDIENTE_ABONO) {
            throw new IllegalStateException("El pedido ya no está pendiente de abono");
        }
        this.total = nuevoTotal;
    }

    public void registrarPago(BigDecimal monto, String referencia) {
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("El abono debe ser mayor que cero");
        }
        if (total.signum() <= 0) {
            throw new IllegalStateException("Primero debe fijarse el total del pedido");
        }
        this.montoAbonado = this.montoAbonado.add(monto);
        this.referenciaPago = referencia;
    }

    public void validarAbono() {
        if (estado != EstadoPedido.PENDIENTE_ABONO) {
            throw new IllegalStateException("El pedido no está pendiente de abono");
        }
        if (!cumpleAbonoMinimo()) {
            throw new IllegalStateException("El abono mínimo es el 50 % del total ("
                    + getAbonoMinimoRequerido().stripTrailingZeros().toPlainString() + ")");
        }
        this.abonoValidado = true;
        this.estado = EstadoPedido.EN_COLA;
    }

    public BigDecimal getSaldoPendiente() {
        return total.subtract(montoAbonado);
    }

    public BigDecimal getAbonoMinimoRequerido() {
        return total.multiply(PORCENTAJE_ABONO_MINIMO).setScale(2, RoundingMode.HALF_UP);
    }

    public boolean cumpleAbonoMinimo() {
        return montoAbonado.compareTo(getAbonoMinimoRequerido()) >= 0;
    }

    public boolean puedeEntregarse() {
        return getSaldoPendiente().compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean cumpleAnticipacion(LocalDateTime ahora) {
        return !fechaEntrega.isBefore(ahora.plusHours(24));
    }

    public void definirPersonalizacion(String sabor, String relleno, String cobertura, String dedicatoria,
                                       Integer porciones, String notas) {
        this.saborBizcocho = sabor;
        this.relleno = relleno;
        this.cobertura = cobertura;
        this.dedicatoria = dedicatoria;
        this.porciones = porciones;
        this.notasAdicionales = notas;
    }

    public void asignarCodigo() {
        this.codigoPedido = "CT-" + fechaCreacion.getYear() + "-" + String.format("%04d", id);
    }
}
