package com.peluchin.bff_service.controller;

import com.peluchin.bff_service.client.ProductoClient;
import com.peluchin.bff_service.dto.ProductoResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
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

}
