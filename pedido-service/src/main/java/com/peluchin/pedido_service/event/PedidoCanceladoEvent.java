package com.peluchin.pedido_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoCanceladoEvent implements Serializable {
    private Long pedidoId;
    private String usuarioSub;
    private LocalDateTime fechaCancelacion;
    private List<PedidoItemEvent> items;
}
