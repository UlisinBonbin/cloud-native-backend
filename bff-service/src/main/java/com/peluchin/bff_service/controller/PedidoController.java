package com.peluchin.bff_service.controller;
import com.peluchin.bff_service.client.PedidoClient;
import com.peluchin.bff_service.dto.AgregarProductoRequest;
import com.peluchin.bff_service.dto.CambiarEstadoRequest;
import com.peluchin.bff_service.dto.PedidoResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PutMapping("/carrito/productos/{productoId}")
    public PedidoResponse actualizarCantidad(
            @PathVariable Long productoId,
            @RequestBody AgregarProductoRequest request) {

        return pedidoClient.actualizarCantidad(
                productoId,
                request
        );
    }

    @DeleteMapping("/carrito/productos/{productoId}")
    public PedidoResponse eliminarProducto(
            @PathVariable Long productoId) {

        return pedidoClient.eliminarProducto(productoId);
    }

    @PostMapping("/carrito/comprar")
    public PedidoResponse comprarCarrito() {
        return pedidoClient.comprarCarrito();
    }

    @GetMapping("/mis-pedidos")
    public List<PedidoResponse> obtenerMisPedidos() {

        return pedidoClient.obtenerMisPedidos();
    }
    @GetMapping
    public List<PedidoResponse> obtenerTodosLosPedidos() {
        return pedidoClient.obtenerTodosLosPedidos();
    }

    @PutMapping("/{id}/estado")
    public PedidoResponse cambiarEstado(
            @PathVariable Long id,
            @RequestBody CambiarEstadoRequest request) {

        return pedidoClient.cambiarEstado(
                id,
                request.getEstado()
        );
    }



}
