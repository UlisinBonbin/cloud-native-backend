package com.peluchin.admin_service.controller;

import com.peluchin.admin_service.dto.CrearExchangeRequest;
import com.peluchin.admin_service.dto.MensajeRespuesta;
import com.peluchin.admin_service.service.RabbitAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints REST para administración de exchanges en RabbitMQ.
 * Cumple con el Indicador 6 y 8 de la evaluación.
 * 
 * Expone rutas compatibles tanto con /exchanges como con /api/v1/admin/exchanges.
 */
@RestController
@RequestMapping({"/exchanges", "/api/v1/admin/exchanges"})
public class ExchangeAdminController {

    private final RabbitAdminService rabbitAdminService;

    public ExchangeAdminController(RabbitAdminService rabbitAdminService) {
        this.rabbitAdminService = rabbitAdminService;
    }

    /**
     * Crea un nuevo exchange en RabbitMQ con validación de parámetros de entrada.
     * Ejemplo: POST /exchanges
     */
    @PostMapping
    public ResponseEntity<MensajeRespuesta> crearExchange(@Valid @RequestBody CrearExchangeRequest request) {
        MensajeRespuesta respuesta = rabbitAdminService.crearExchange(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /**
     * Elimina un exchange existente en RabbitMQ.
     * Ejemplo: DELETE /exchanges/{name}
     */
    @DeleteMapping("/{name}")
    public ResponseEntity<MensajeRespuesta> eliminarExchange(@PathVariable String name) {
        boolean eliminado = rabbitAdminService.eliminarExchange(name);
        if (eliminado) {
            return ResponseEntity.ok(MensajeRespuesta.exito("Exchange '" + name + "' eliminado exitosamente", null));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(MensajeRespuesta.error(404, "El exchange '" + name + "' no pudo ser eliminado o no existe", null));
        }
    }
}
