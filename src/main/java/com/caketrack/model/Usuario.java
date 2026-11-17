package com.caketrack.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Usuario {

    public static final Duration VIGENCIA_TOKEN = Duration.ofMinutes(30);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank
    @Email
    @Size(max = 150)
    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String password;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "token_recuperacion", length = 255)
    private String tokenRecuperacion;

    @Column(name = "token_expiracion")
    private LocalDateTime tokenExpiracion;

    @Column(name = "token_usado", nullable = false)
    private boolean tokenUsado = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Usuario(String nombre, String correo, String passwordCifrada, Rol rol) {
        this.nombre = nombre;
        this.correo = correo;
        this.password = passwordCifrada;
        this.rol = rol;
    }

    public void actualizarDatos(String nombre, String correo, Rol rol) {
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }

    public void cambiarPassword(String passwordCifrada) {
        this.password = passwordCifrada;
        this.tokenRecuperacion = null;
        this.tokenExpiracion = null;
        this.tokenUsado = true;
    }

    public void generarTokenRecuperacion(String token, LocalDateTime ahora) {
        this.tokenRecuperacion = token;
        this.tokenExpiracion = ahora.plus(VIGENCIA_TOKEN);
        this.tokenUsado = false;
    }

    public boolean tokenExpirado(LocalDateTime instante) {
        return tokenExpiracion == null || !instante.isBefore(tokenExpiracion);
    }

    public boolean tokenValido(LocalDateTime instante) {
        return tokenRecuperacion != null && !tokenUsado && !tokenExpirado(instante);
    }
}
