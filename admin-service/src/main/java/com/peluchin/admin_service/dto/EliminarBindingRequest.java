package com.peluchin.admin_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EliminarBindingRequest {

    @NotBlank(message = "El nombre de la cola es obligatorio")
    private String queueName;

    @NotBlank(message = "El nombre del exchange es obligatorio")
    private String exchangeName;

    @NotBlank(message = "La routing key es obligatoria")
    private String routingKey;
}
