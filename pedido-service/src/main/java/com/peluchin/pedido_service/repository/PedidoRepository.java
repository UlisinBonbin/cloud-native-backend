package com.peluchin.pedido_service.repository;

import com.peluchin.pedido_service.enums.EstadoPedido;
import com.peluchin.pedido_service.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    Optional<Pedido> findByUsuarioSubAndEstado(
            String usuarioSub,
            EstadoPedido estado

    );
}
