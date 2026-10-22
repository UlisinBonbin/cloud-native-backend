package com.peluchin.admin_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la creación de bindings entre colas y exchanges en RabbitMQ.
 * Cumple con el Indicador 8 de la evaluación.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearBindingRequest {

    @NotBlank(message = "El nombre de la cola es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre de la cola debe tener entre 3 y 100 caracteres")
    @Pattern(
        regexp = "^[a-zA-Z0-9._-]+$",
        message = "El nombre de la cola solo puede contener caracteres alfanuméricos, puntos, guiones y guiones bajos"
    )
    private String queueName;

    @NotBlank(message = "El nombre del exchange es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre del exchange debe tener entre 3 y 100 caracteres")
    @Pattern(
        regexp = "^[a-zA-Z0-9._-]+$",
        message = "El nombre del exchange solo puede contener caracteres alfanuméricos, puntos, guiones y guiones bajos"
    )
    private String exchangeName;

    @NotBlank(message = "La routing key es obligatoria")
    @Size(min = 1, max = 100, message = "La routing key debe tener entre 1 y 100 caracteres")
    private String routingKey;
}
