package com.peluchin.bff_service.client;
import com.peluchin.bff_service.dto.AgregarProductoRequest;
import com.peluchin.bff_service.dto.PedidoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PedidoClient {

    private final RestClient restClient;

    public PedidoClient(
            RestClient.Builder builder,
            ClientHttpRequestInterceptor bearerTokenInterceptor,
            @Value("${pedido-service.url}") String pedidoServiceUrl) {

        this.restClient = builder
                .baseUrl(pedidoServiceUrl)
                .requestInterceptor(bearerTokenInterceptor)
                .build();
    }

    public PedidoResponse obtenerCarrito() {

        return restClient.get()
                .uri("/api/v1/pedidos/carrito")
                .retrieve()
                .body(PedidoResponse.class);
    }

    public PedidoResponse agregarProducto(
            AgregarProductoRequest request) {

        return restClient.post()
                .uri("/api/v1/pedidos/carrito/productos")
                .body(request)
                .retrieve()
                .body(PedidoResponse.class);
    }

    public PedidoResponse actualizarCantidad(
            Long productoId,
            AgregarProductoRequest request) {

        return restClient.put()
                .uri(
                        "/api/v1/pedidos/carrito/productos/{productoId}",
                        productoId
                )
                .body(request)
                .retrieve()
                .body(PedidoResponse.class);
    }

    public PedidoResponse eliminarProducto(
            Long productoId) {

        return restClient.delete()
                .uri(
                        "/api/v1/pedidos/carrito/productos/{productoId}",
                        productoId
                )
                .retrieve()
                .body(PedidoResponse.class);
    }

    public PedidoResponse comprarCarrito() {

        return restClient.post()
                .uri("/api/v1/pedidos/carrito/comprar")
                .retrieve()
                .body(PedidoResponse.class);
    }

}
