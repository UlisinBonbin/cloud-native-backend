package com.peluchin.admin_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la creación de exchanges en RabbitMQ con validaciones de entrada.
 * Cumple con el Indicador 8 de la evaluación.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearExchangeRequest {

    @NotBlank(message = "El nombre del exchange no puede estar vacío")
    @Size(min = 3, max = 100, message = "El nombre del exchange debe tener entre 3 y 100 caracteres")
    @Pattern(
        regexp = "^[a-zA-Z0-9._-]+$",
        message = "El nombre del exchange solo puede contener caracteres alfanuméricos, puntos, guiones y guiones bajos"
    )
    private String nombre;

    @NotBlank(message = "El tipo de exchange es obligatorio")
    @Pattern(
        regexp = "^(?i)(direct|topic|fanout|headers)$",
        message = "El tipo de exchange debe ser 'direct', 'topic', 'fanout' o 'headers'"
    )
    private String tipo;

    @NotNull(message = "El campo 'durable' es obligatorio (true/false)")
    private Boolean durable;

    private Boolean autoDelete;
}
