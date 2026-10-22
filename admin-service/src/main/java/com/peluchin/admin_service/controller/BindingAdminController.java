package com.peluchin.admin_service.controller;

import com.peluchin.admin_service.dto.CrearBindingRequest;
import com.peluchin.admin_service.dto.EliminarBindingRequest;
import com.peluchin.admin_service.dto.MensajeRespuesta;
import com.peluchin.admin_service.service.RabbitAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints REST para administración de bindings (enlaces) en RabbitMQ.
 * Cumple con el Indicador 6 y 8 de la evaluación.
 * 
 * Expone rutas compatibles tanto con /bindings como con /api/v1/admin/bindings.
 */
@RestController
@RequestMapping({"/bindings", "/api/v1/admin/bindings"})
public class BindingAdminController {

    private final RabbitAdminService rabbitAdminService;

    public BindingAdminController(RabbitAdminService rabbitAdminService) {
        this.rabbitAdminService = rabbitAdminService;
    }

    /**
     * Crea un nuevo binding entre cola y exchange.
     * Ejemplo: POST /bindings
     */
    @PostMapping
    public ResponseEntity<MensajeRespuesta> crearBinding(@Valid @RequestBody CrearBindingRequest request) {
        MensajeRespuesta respuesta = rabbitAdminService.crearBinding(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /**
     * Elimina un binding existente.
     * Ejemplo: DELETE /bindings
     */
    @DeleteMapping
    public ResponseEntity<MensajeRespuesta> eliminarBinding(@Valid @RequestBody EliminarBindingRequest request) {
        rabbitAdminService.eliminarBinding(request);
        return ResponseEntity.ok(MensajeRespuesta.exito("Binding eliminado exitosamente", null));
    }
}
