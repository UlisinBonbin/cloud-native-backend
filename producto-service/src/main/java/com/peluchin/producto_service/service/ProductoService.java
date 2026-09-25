package com.peluchin.producto_service.service;

import com.peluchin.producto_service.model.Producto;
import com.peluchin.producto_service.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> getAllProductos() {
        return productoRepository.findAll();
    }

    public Producto getProductoById(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    public Producto saveProducto(Producto producto){
        return productoRepository.save(producto);
    }

    public void deleteProducto(Long id){
        productoRepository.deleteById(id);
    }

    public Producto updateProducto(Long id, Producto productoActualizado){
        return productoRepository.findById(id)
                .map(producto -> {
                    producto.setNombre(productoActualizado.getNombre());
                    producto.setPrecio(productoActualizado.getPrecio());
                    producto.setImagenUrl(productoActualizado.getImagenUrl());
                    producto.setStock(productoActualizado.getStock());
                    return productoRepository.save(producto);
                })
                .orElse(null);
    }

    public Producto descontarStock(Long id, Integer cantidad) {

        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que 0"
            );
        }

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Producto no encontrado"
                        )
                );

        if (producto.getStock() < cantidad) {
            throw new RuntimeException(
                    "No hay suficiente stock"
            );
        }

        producto.setStock(
                producto.getStock() - cantidad
        );

        return productoRepository.save(producto);
    }

}
