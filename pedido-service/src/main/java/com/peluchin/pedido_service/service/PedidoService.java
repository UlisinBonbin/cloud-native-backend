package com.peluchin.pedido_service.service;

import com.peluchin.pedido_service.client.ProductoClient;
import com.peluchin.pedido_service.dto.AgregarProductoRequest;
import com.peluchin.pedido_service.dto.ProductoResponse;
import com.peluchin.pedido_service.enums.EstadoPedido;
import com.peluchin.pedido_service.model.Pedido;
import com.peluchin.pedido_service.model.PedidoItem;
import com.peluchin.pedido_service.repository.PedidoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PedidoService {
    private final PedidoRepository pedidoRepository;
    private final ProductoClient productoClient;

    public PedidoService(
            PedidoRepository pedidoRepository,
            ProductoClient productoClient) {

        this.pedidoRepository = pedidoRepository;
        this.productoClient = productoClient;
    }

    public List<Pedido> getAllPedidos() {
        return pedidoRepository.findAll();
    }

    public Pedido getPedidoById(Long id) {
        return pedidoRepository.findById(id)
                .orElse(null);
    }

    public Pedido getCarritoByUsuario(String usuarioSub) {

        return pedidoRepository
                .findByUsuarioSubAndEstado(
                        usuarioSub,
                        EstadoPedido.CARRITO
                )
                .orElseGet(() -> {

                    Pedido pedido = new Pedido();

                    pedido.setUsuarioSub(usuarioSub);
                    pedido.setEstado(EstadoPedido.CARRITO);
                    pedido.setFechaCreacion(LocalDateTime.now());

                    return pedidoRepository.save(pedido);
                });
    }

    @Transactional
    public Pedido agregarProductoAlCarrito(
            String usuarioSub,
            AgregarProductoRequest request,
            String token) {

        if (request.getCantidad() == null ||
                request.getCantidad() <= 0) {

            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que 0"
            );
        }

        ProductoResponse producto =
                productoClient.getProductoById(
                        request.getProductoId(),
                        token
                );

        if (producto == null) {
            throw new RuntimeException(
                    "Producto no encontrado"
            );
        }

        Pedido carrito =
                getCarritoByUsuario(usuarioSub);

        PedidoItem itemExistente = carrito.getItems()
                .stream()
                .filter(item ->
                        item.getProductoId()
                                .equals(producto.getId())
                )
                .findFirst()
                .orElse(null);

        if (itemExistente != null) {

            int nuevaCantidad =
                    itemExistente.getCantidad()
                            + request.getCantidad();

            if (nuevaCantidad > producto.getStock()) {
                throw new RuntimeException(
                        "No hay suficiente stock"
                );
            }

            itemExistente.setCantidad(nuevaCantidad);

        } else {

            if (request.getCantidad() > producto.getStock()) {
                throw new RuntimeException(
                        "No hay suficiente stock"
                );
            }

            PedidoItem nuevoItem = new PedidoItem();

            nuevoItem.setPedido(carrito);
            nuevoItem.setProductoId(producto.getId());
            nuevoItem.setNombreProducto(producto.getNombre());
            nuevoItem.setPrecioUnitario(producto.getPrecio());
            nuevoItem.setCantidad(request.getCantidad());

            carrito.getItems().add(nuevoItem);
        }

        return pedidoRepository.save(carrito);
    }

    @Transactional
    public Pedido actualizarCantidadProducto(
            String usuarioSub,
            Long productoId,
            Integer cantidad,
            String token) {

        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que 0"
            );
        }

        Pedido carrito = getCarritoByUsuario(usuarioSub);

        PedidoItem item = carrito.getItems()
                .stream()
                .filter(i ->
                        i.getProductoId().equals(productoId)
                )
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException(
                                "El producto no está en el carrito"
                        )
                );

        ProductoResponse producto =
                productoClient.getProductoById(
                        productoId,
                        token
                );

        if (producto == null) {
            throw new RuntimeException(
                    "Producto no encontrado"
            );
        }

        if (cantidad > producto.getStock()) {
            throw new RuntimeException(
                    "No hay suficiente stock"
            );
        }

        item.setCantidad(cantidad);

        return pedidoRepository.save(carrito);
    }

    @Transactional
    public Pedido eliminarProductoDelCarrito(
            String usuarioSub,
            Long productoId) {

        Pedido carrito = getCarritoByUsuario(usuarioSub);

        boolean eliminado = carrito.getItems()
                .removeIf(item ->
                        item.getProductoId().equals(productoId)
                );

        if (!eliminado) {
            throw new RuntimeException(
                    "El producto no está en el carrito"
            );
        }

        return pedidoRepository.save(carrito);
    }

    @Transactional
    public Pedido comprarCarrito(
            String usuarioSub,
            String token) {

        Pedido carrito = pedidoRepository
                .findByUsuarioSubAndEstado(
                        usuarioSub,
                        EstadoPedido.CARRITO
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "No existe un carrito"
                        )
                );

        if (carrito.getItems().isEmpty()) {
            throw new RuntimeException(
                    "El carrito está vacío"
            );
        }

        // Volvemos a comprobar el stock antes de comprar.
        for (PedidoItem item : carrito.getItems()) {

            ProductoResponse producto =
                    productoClient.getProductoById(
                            item.getProductoId(),
                            token
                    );

            if (producto == null) {
                throw new RuntimeException(
                        "Producto no encontrado: "
                                + item.getProductoId()
                );
            }

            if (item.getCantidad() > producto.getStock()) {
                throw new RuntimeException(
                        "Stock insuficiente para: "
                                + item.getNombreProducto()
                );
            }
        }

        carrito.setEstado(EstadoPedido.PAGADO);

        return pedidoRepository.save(carrito);
    }
}

