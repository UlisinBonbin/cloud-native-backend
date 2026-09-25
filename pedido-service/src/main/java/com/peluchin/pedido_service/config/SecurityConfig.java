package com.peluchin.pedido_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CognitoJwtAuthenticationConverter jwtAuthenticationConverter;

    public SecurityConfig(
            CognitoJwtAuthenticationConverter jwtAuthenticationConverter) {

        this.jwtAuthenticationConverter =
                jwtAuthenticationConverter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        // Carrito y pedidos propios
                        .requestMatchers(
                                "/api/v1/pedidos/carrito/**"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/pedidos/mis-pedidos"
                        ).authenticated()

                        // Gestión completa de pedidos
                        // SOLO OPERADOR
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/pedidos"
                        ).hasRole("OPERADOR")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/pedidos/*/estado"
                        ).hasRole("OPERADOR")

                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter
                                )
                        )
                );

        return http.build();
    }

}
