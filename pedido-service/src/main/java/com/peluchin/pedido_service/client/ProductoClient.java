package com.peluchin.pedido_service.client;

import com.peluchin.pedido_service.dto.ProductoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

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

    public ProductoResponse getProductoById(
            Long productoId,
            String token) {

        return restClient.get()
                .uri("/api/v1/productos/{id}", productoId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(ProductoResponse.class);
    }

    public ProductoResponse descontarStock(
            Long productoId,
            Integer cantidad,
            String token) {

        return restClient.post()
                .uri("/api/v1/productos/{id}/stock", productoId)
                .header(
                        "Authorization",
                        "Bearer " + token
                )
                .body(
                        Map.of(
                                "cantidad",
                                cantidad
                        )
                )
                .retrieve()
                .body(ProductoResponse.class);
    }


}
