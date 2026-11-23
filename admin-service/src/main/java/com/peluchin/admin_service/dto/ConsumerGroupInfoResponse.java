package com.peluchin.admin_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO de respuesta con la inspección del Consumer Group, miembros y cálculo de LAG.
 * Cumple con los Indicadores 8 y 9.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumerGroupInfoResponse {

    private String groupId;
    private String estado;
    private int cantidadMiembros;
    private List<String> miembros;
    private Map<String, Long> offsetsPorParticion;
    private Map<String, Long> lagPorParticion;
    private long totalLag;
}
