package com.peluchin.pedido_service.service;

import com.peluchin.pedido_service.client.ProductoClient;
import com.peluchin.pedido_service.dto.AgregarProductoRequest;
import com.peluchin.pedido_service.dto.ProductoResponse;
import com.peluchin.pedido_service.enums.EstadoPedido;
import com.peluchin.pedido_service.model.Pedido;
import com.peluchin.pedido_service.model.PedidoItem;
import com.peluchin.pedido_service.repository.PedidoRepository;
import org.springframework.stereotype.Service;

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

        if (producto.getStock() < request.getCantidad()) {
            throw new RuntimeException(
                    "No hay suficiente stock"
            );
        }

        Pedido carrito =
                getCarritoByUsuario(usuarioSub);

        PedidoItem item = new PedidoItem();

        item.setPedido(carrito);
        item.setProductoId(producto.getId());
        item.setNombreProducto(producto.getNombre());
        item.setPrecioUnitario(producto.getPrecio());
        item.setCantidad(request.getCantidad());

        carrito.getItems().add(item);

        return pedidoRepository.save(carrito);
    }
}

