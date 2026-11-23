package com.peluchin.admin_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO de respuesta con la información detallada de un tópico Kafka.
 * Cumple con los Indicadores 3, 7 y 9.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicoInfoResponse {

    private String nombre;
    private int particiones;
    private int factorReplicacion;
    private Map<String, String> configuraciones;
}
