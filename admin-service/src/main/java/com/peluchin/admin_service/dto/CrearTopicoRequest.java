package com.peluchin.admin_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de solicitud para creación de un tópico en Kafka con parámetros avanzados.
 * Cumple con los Indicadores 3 y 7.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearTopicoRequest {

    @NotBlank(message = "El nombre del tópico es obligatorio")
    private String nombre;

    @Builder.Default
    private Integer particiones = 3;

    @Builder.Default
    private Short replicas = 1;

    @Builder.Default
    private Long retentionMs = 604800000L; // 7 días por defecto

    @Builder.Default
    private String cleanupPolicy = "delete";
}
