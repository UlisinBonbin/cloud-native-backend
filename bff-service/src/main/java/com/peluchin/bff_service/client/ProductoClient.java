package com.peluchin.bff_service.client;

import com.peluchin.bff_service.dto.ProductoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class ProductoClient {

    private final RestClient restClient;

    public ProductoClient(
            RestClient.Builder builder,
            @Value("${producto-service.url}") String productoServiceUrl) {

        this.restClient = builder
                .baseUrl(productoServiceUrl)
                .build();
    }

    public List<ProductoResponse> obtenerProductos() {

        return restClient.get()
                .uri("/api/v1/productos")
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<>() {});
    }

    public ProductoResponse obtenerProducto(Long id) {

        return restClient.get()
                .uri("/api/v1/productos/{id}", id)
                .retrieve()
                .body(ProductoResponse.class);
    }

}
