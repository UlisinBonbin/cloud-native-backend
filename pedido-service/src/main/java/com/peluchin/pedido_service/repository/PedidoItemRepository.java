package com.peluchin.pedido_service.repository;

import com.peluchin.pedido_service.model.PedidoItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoItemRepository  extends JpaRepository<PedidoItem, Long> {
}
