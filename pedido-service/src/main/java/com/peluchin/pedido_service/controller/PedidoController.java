package com.peluchin.pedido_service.controller;

import com.peluchin.pedido_service.dto.AgregarProductoRequest;
import com.peluchin.pedido_service.dto.CambiarEstadoRequest;
import com.peluchin.pedido_service.model.Pedido;
import com.peluchin.pedido_service.service.PedidoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public List<Pedido> obtenerTodosLosPedidos() {
        return pedidoService.getAllPedidos();
    }

    @PutMapping("/{id}/estado")
    public Pedido cambiarEstado(
            @PathVariable Long id,
            @RequestBody CambiarEstadoRequest request) {

        return pedidoService.cambiarEstadoPedido(
                id,
                request.getEstado()
        );
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

    @PutMapping("/carrito/productos/{productoId}")
    public Pedido actualizarCantidad(
            @PathVariable Long productoId,
            @RequestBody AgregarProductoRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        return pedidoService.actualizarCantidadProducto(
                jwt.getSubject(),
                productoId,
                request.getCantidad(),
                jwt.getTokenValue()
        );
    }

    @DeleteMapping("/carrito/productos/{productoId}")
    public Pedido eliminarProducto(
            @PathVariable Long productoId,
            @AuthenticationPrincipal Jwt jwt) {

        return pedidoService.eliminarProductoDelCarrito(
                jwt.getSubject(),
                productoId
        );
    }

    @PostMapping("/carrito/comprar")
    public Pedido comprarCarrito(
            @AuthenticationPrincipal Jwt jwt) {

        return pedidoService.comprarCarrito(
                jwt.getSubject(),
                jwt.getTokenValue()
        );
    }

    @GetMapping("/mis-pedidos")
    public List<Pedido> getMisPedidos(
            @AuthenticationPrincipal Jwt jwt) {

        return pedidoService.getPedidosByUsuario(
                jwt.getSubject()
        );
    }


}
