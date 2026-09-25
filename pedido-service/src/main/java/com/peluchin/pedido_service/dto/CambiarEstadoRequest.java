package com.peluchin.pedido_service.dto;

import com.peluchin.pedido_service.enums.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CambiarEstadoRequest {

    private EstadoPedido estado;
}
