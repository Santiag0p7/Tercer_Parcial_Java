package com.caketrack.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "receta_items",
        uniqueConstraints = @UniqueConstraint(columnNames = {"producto_id", "ingrediente_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecetaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ingrediente_id", nullable = false)
    private Ingrediente ingrediente;

    @NotNull
    @Positive
    @Digits(integer = 8, fraction = 2)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;

    RecetaItem(Producto producto, Ingrediente ingrediente, BigDecimal cantidad) {
        this.producto = producto;
        this.ingrediente = ingrediente;
        this.cantidad = cantidad;
    }

    public void cambiarCantidad(BigDecimal nuevaCantidad) {
        this.cantidad = nuevaCantidad;
    }
}
