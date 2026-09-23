package com.peluchin.bff_service.controller;
import com.peluchin.bff_service.client.PedidoClient;
import com.peluchin.bff_service.dto.AgregarProductoRequest;
import com.peluchin.bff_service.dto.PedidoResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pedidos")

public class PedidoController {

    private final PedidoClient pedidoClient;

    public PedidoController(PedidoClient pedidoClient) {
        this.pedidoClient = pedidoClient;
    }

    @GetMapping("/carrito")
    public PedidoResponse obtenerCarrito() {
        return pedidoClient.obtenerCarrito();
    }

    @PostMapping("/carrito/productos")
    public PedidoResponse agregarProducto(
            @RequestBody AgregarProductoRequest request) {

        return pedidoClient.agregarProducto(request);
    }

}
