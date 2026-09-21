package com.peluchin.bff_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class PedidoResponse {

    private Long id;
    private String usuarioSub;
    private String estado;
    private LocalDateTime fechaCreacion;
    private List<PedidoItemResponse> items;

}
