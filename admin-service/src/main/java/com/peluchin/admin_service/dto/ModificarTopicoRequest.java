package com.peluchin.admin_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de solicitud para modificar configuraciones o particiones de un tópico.
 * Cumple con los Indicadores 7 y 9.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModificarTopicoRequest {

    /**
     * Nuevo número total de particiones (solo puede incrementarse).
     */
    private Integer particiones;

    /**
     * Nuevo tiempo de retención en milisegundos.
     */
    private Long retentionMs;

    /**
     * Nueva política de limpieza (ej. "delete", "compact").
     */
    private String cleanupPolicy;
}
