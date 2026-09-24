package com.peluchin.bff_service.controller;

import com.peluchin.bff_service.client.ProductoClient;
import com.peluchin.bff_service.dto.ProductoRequest;
import com.peluchin.bff_service.dto.ProductoResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final ProductoClient productoClient;

    public ProductoController(ProductoClient productoClient) {
        this.productoClient = productoClient;
    }

    @GetMapping
    public List<ProductoResponse> obtenerProductos() {
        return productoClient.obtenerProductos();
    }

    @GetMapping("/{id}")
    public ProductoResponse obtenerProducto(@PathVariable Long id) {
        return productoClient.obtenerProducto(id);
    }

    @PostMapping
    public ProductoResponse crearProducto(
            @RequestBody ProductoRequest producto) {

        return productoClient.crearProducto(producto);
    }

    @PutMapping("/{id}")
    public ProductoResponse actualizarProducto(
            @PathVariable Long id,
            @RequestBody ProductoRequest producto) {

        return productoClient.actualizarProducto(id, producto);
    }

    @DeleteMapping("/{id}")
    public void eliminarProducto(@PathVariable Long id) {

        productoClient.eliminarProducto(id);
    }

}
