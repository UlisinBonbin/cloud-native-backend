package com.peluchin.pedido_service.controller;

import com.peluchin.pedido_service.dto.AgregarProductoRequest;
import com.peluchin.pedido_service.model.Pedido;
import com.peluchin.pedido_service.service.PedidoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping("/carrito")
    public Pedido getCarrito(
            @AuthenticationPrincipal Jwt jwt) {

        return pedidoService.getCarritoByUsuario(
                jwt.getSubject()
        );
    }

    @PostMapping("/carrito/productos")
    public Pedido agregarProducto(
            @RequestBody AgregarProductoRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        return pedidoService.agregarProductoAlCarrito(
                jwt.getSubject(),
                request,
                jwt.getTokenValue()
        );
    }

}
