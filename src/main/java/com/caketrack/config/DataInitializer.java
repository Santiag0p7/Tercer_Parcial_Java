package com.caketrack.config;

import com.caketrack.model.Ingrediente;
import com.caketrack.model.Producto;
import com.caketrack.model.Rol;
import com.caketrack.model.TipoProducto;
import com.caketrack.model.Usuario;
import com.caketrack.repository.IngredienteRepository;
import com.caketrack.repository.ProductoRepository;
import com.caketrack.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final String PASSWORD_PRUEBA = "Password123!";

    private final UsuarioRepository usuarioRepository;
    private final IngredienteRepository ingredienteRepository;
    private final ProductoRepository productoRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           IngredienteRepository ingredienteRepository,
                           ProductoRepository productoRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.ingredienteRepository = ingredienteRepository;
        this.productoRepository = productoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            sembrarUsuarios();
        }
        if (ingredienteRepository.count() == 0) {
            sembrarIngredientes();
        }
        if (productoRepository.count() == 0) {
            sembrarProductos();
        }
    }

    private void sembrarUsuarios() {
        guardarUsuario("Administrador", "admin@caketrack.com", Rol.ADMIN);
        guardarUsuario("Cajero de prueba", "cajero@caketrack.com", Rol.CAJERO);
        guardarUsuario("Pastelero de prueba", "pastelero@caketrack.com", Rol.PASTELERO);
        guardarUsuario("Repartidor de prueba", "repartidor@caketrack.com", Rol.REPARTIDOR);
        guardarUsuario("Cliente de prueba", "cliente@caketrack.com", Rol.CLIENTE);
    }

    private void guardarUsuario(String nombre, String correo, Rol rol) {
        usuarioRepository.save(new Usuario(nombre, correo, passwordEncoder.encode(PASSWORD_PRUEBA), rol));
    }

    private void sembrarIngredientes() {
        ingredienteRepository.save(new Ingrediente("Harina de almendras", "kg",
                new BigDecimal("5.00"), new BigDecimal("2.00")));
        ingredienteRepository.save(new Ingrediente("Chocolate semiamargo", "kg",
                new BigDecimal("1.50"), new BigDecimal("2.00")));
        ingredienteRepository.save(new Ingrediente("Crema de leche", "L",
                new BigDecimal("3.00"), new BigDecimal("1.00")));
        ingredienteRepository.save(new Ingrediente("Fresas", "kg",
                new BigDecimal("0.80"), new BigDecimal("1.00")));
    }

    private void sembrarProductos() {
        Ingrediente harina = buscarIngrediente("Harina de almendras");
        Ingrediente chocolate = buscarIngrediente("Chocolate semiamargo");
        Ingrediente crema = buscarIngrediente("Crema de leche");
        Ingrediente fresas = buscarIngrediente("Fresas");

        Producto torta = new Producto("Torta de almendras", TipoProducto.TORTA,
                new BigDecimal("65000.00"), "Torta de almendras con cobertura de chocolate semiamargo.");
        torta.agregarIngrediente(harina, new BigDecimal("0.40"));
        torta.agregarIngrediente(chocolate, new BigDecimal("0.20"));
        torta.agregarIngrediente(crema, new BigDecimal("0.50"));
        torta.marcarDisponible();
        productoRepository.save(torta);

        Producto postre = new Producto("Postre de fresas con crema", TipoProducto.POSTRE,
                new BigDecimal("18000.00"), "Copa de fresas frescas con crema de leche.");
        postre.agregarIngrediente(fresas, new BigDecimal("0.15"));
        postre.agregarIngrediente(crema, new BigDecimal("0.20"));
        postre.marcarDisponible();
        productoRepository.save(postre);
    }

    private Ingrediente buscarIngrediente(String nombre) {
        return ingredienteRepository.findByNombre(nombre)
                .orElseThrow(() -> new IllegalStateException("Ingrediente semilla no encontrado: " + nombre));
    }
}
