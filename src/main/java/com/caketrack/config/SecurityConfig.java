package com.caketrack.config;

import com.caketrack.repository.UsuarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/recuperar", "/restablecer", "/css/**", "/js/**", "/img/**", "/error").permitAll()
                .requestMatchers("/admin/usuarios/**").hasRole("ADMIN")
                .requestMatchers("/admin/ingredientes/**", "/admin/productos/**").hasAnyRole("ADMIN", "PASTELERO")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/caja/**").hasAnyRole("CAJERO", "ADMIN")
                .requestMatchers("/cocina/**").hasAnyRole("PASTELERO", "ADMIN")
                .requestMatchers("/domicilios/**").hasAnyRole("REPARTIDOR", "ADMIN")
                .requestMatchers("/inicio", "/catalogo/**", "/mis-pedidos/**").authenticated()
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/inicio", true)
                .failureHandler((request, response, exception) ->
                        response.sendRedirect(exception instanceof DisabledException
                                ? "/login?inactivo"
                                : "/login?error"))
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll());
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(UsuarioRepository usuarioRepository) {
        return correo -> usuarioRepository.findByCorreo(correo)
                .map(usuario -> User.withUsername(usuario.getCorreo())
                        .password(usuario.getPassword())
                        .roles(usuario.getRol().name())
                        .disabled(!usuario.isActivo())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SpringSecurityDialect springSecurityDialect() {
        return new SpringSecurityDialect();
    }
}
