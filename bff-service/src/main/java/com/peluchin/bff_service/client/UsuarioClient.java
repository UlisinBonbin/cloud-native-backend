package com.peluchin.bff_service.client;

import com.peluchin.bff_service.dto.UsuarioRequest;
import com.peluchin.bff_service.dto.UsuarioResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UsuarioClient {

    private final RestClient restClient;

    public UsuarioClient(
            RestClient.Builder builder,
            @Value("${usuario-service.url}") String usuarioServiceUrl) {

        this.restClient = builder
                .baseUrl(usuarioServiceUrl)
                .build();
    }

    public UsuarioResponse obtenerUsuarioActual() {

        return restClient.get()
                .uri("/api/v1/usuarios/me")
                .retrieve()
                .body(UsuarioResponse.class);
    }

    public UsuarioResponse crearUsuario(UsuarioRequest request) {

        return restClient.post()
                .uri("/api/v1/usuarios/me")
                .body(request)
                .retrieve()
                .body(UsuarioResponse.class);
    }

    public UsuarioResponse actualizarUsuario(UsuarioRequest request) {

        return restClient.put()
                .uri("/api/v1/usuarios/me")
                .body(request)
                .retrieve()
                .body(UsuarioResponse.class);
    }
}
