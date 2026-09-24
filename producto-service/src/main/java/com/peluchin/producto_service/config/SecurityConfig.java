package com.peluchin.producto_service.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
public class SecurityConfig {

    private final CognitoJwtAuthenticationConverter jwtAuthenticationConverter;

    public SecurityConfig(
            CognitoJwtAuthenticationConverter jwtAuthenticationConverter) {
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        // Catálogo público
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/productos",
                                "/api/v1/productos/**"
                        ).permitAll()

                        // Solo administrador
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/productos",
                                "/api/v1/productos/**"
                        ).hasRole("ADMINISTRADOR")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/productos/**"
                        ).hasRole("ADMINISTRADOR")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/productos/**"
                        ).hasRole("ADMINISTRADOR")

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
