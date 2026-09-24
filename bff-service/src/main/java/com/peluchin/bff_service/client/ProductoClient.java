package com.peluchin.bff_service.client;

import com.peluchin.bff_service.dto.ProductoRequest;
import com.peluchin.bff_service.dto.ProductoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class ProductoClient {

    private final RestClient restClient;

    public ProductoClient(
            RestClient.Builder builder,
            ClientHttpRequestInterceptor bearerTokenInterceptor,
            @Value("${producto-service.url}") String productoServiceUrl) {

        this.restClient = builder
                .baseUrl(productoServiceUrl)
                .requestInterceptor(bearerTokenInterceptor)
                .build();
    }

    public List<ProductoResponse> obtenerProductos() {

        return restClient.get()
                .uri("/api/v1/productos")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public ProductoResponse obtenerProducto(Long id) {

        return restClient.get()
                .uri("/api/v1/productos/{id}", id)
                .retrieve()
                .body(ProductoResponse.class);
    }

    public ProductoResponse crearProducto(ProductoRequest producto) {

        return restClient.post()
                .uri("/api/v1/productos")
                .body(producto)
                .retrieve()
                .body(ProductoResponse.class);
    }

    public ProductoResponse actualizarProducto(
            Long id,
            ProductoRequest producto) {

        return restClient.put()
                .uri("/api/v1/productos/{id}", id)
                .body(producto)
                .retrieve()
                .body(ProductoResponse.class);
    }

    public void eliminarProducto(Long id) {

        restClient.delete()
                .uri("/api/v1/productos/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }

}
