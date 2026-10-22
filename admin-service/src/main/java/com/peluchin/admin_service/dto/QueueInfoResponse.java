package com.peluchin.admin_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueInfoResponse {
    private String nombre;
    private Integer messageCount;
    private Integer consumerCount;
    private Map<String, Object> argumentos;
}
