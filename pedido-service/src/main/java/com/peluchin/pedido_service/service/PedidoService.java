package com.peluchin.pedido_service.service;

import com.peluchin.pedido_service.client.ProductoClient;
import com.peluchin.pedido_service.dto.AgregarProductoRequest;
import com.peluchin.pedido_service.dto.ProductoResponse;
import com.peluchin.pedido_service.enums.EstadoPedido;
import com.peluchin.pedido_service.model.Pedido;
import com.peluchin.pedido_service.model.PedidoItem;
import com.peluchin.pedido_service.repository.PedidoRepository;
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

        // Buscar si el producto ya existe en el carrito
        PedidoItem itemExistente = carrito.getItems()
                .stream()
                .filter(item ->
                        item.getProductoId()
                                .equals(producto.getId())
                )
                .findFirst()
                .orElse(null);

        if (itemExistente != null) {

            // El producto ya existe:
            // aumentamos la cantidad
            int nuevaCantidad =
                    itemExistente.getCantidad()
                            + request.getCantidad();

            // Validar el stock total acumulado
            if (nuevaCantidad > producto.getStock()) {
                throw new RuntimeException(
                        "No hay suficiente stock"
                );
            }

            itemExistente.setCantidad(nuevaCantidad);

        } else {

            // Producto nuevo en el carrito
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
}

