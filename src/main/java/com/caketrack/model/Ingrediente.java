package com.caketrack.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ingredientes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ingrediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @NotBlank
    @Size(max = 20)
    @Column(name = "unidad_medida", nullable = false, length = 20)
    private String unidadMedida;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 8, fraction = 2)
    @Column(name = "cantidad_actual", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidadActual;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 8, fraction = 2)
    @Column(name = "stock_minimo", nullable = false, precision = 10, scale = 2)
    private BigDecimal stockMinimo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Ingrediente(String nombre, String unidadMedida, BigDecimal cantidadActual, BigDecimal stockMinimo) {
        this.nombre = nombre;
        this.unidadMedida = unidadMedida;
        this.cantidadActual = cantidadActual;
        this.stockMinimo = stockMinimo;
    }

    public void actualizar(String nombre, String unidadMedida, BigDecimal cantidadActual, BigDecimal stockMinimo) {
        this.nombre = nombre;
        this.unidadMedida = unidadMedida;
        this.cantidadActual = cantidadActual;
        this.stockMinimo = stockMinimo;
    }

    public boolean isBajoStockMinimo() {
        return cantidadActual.compareTo(stockMinimo) <= 0;
    }
}
