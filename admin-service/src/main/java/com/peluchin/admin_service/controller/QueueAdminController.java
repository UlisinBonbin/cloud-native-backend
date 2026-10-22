package com.peluchin.admin_service.controller;

import com.peluchin.admin_service.dto.CrearColaRequest;
import com.peluchin.admin_service.dto.MensajeRespuesta;
import com.peluchin.admin_service.dto.QueueInfoResponse;
import com.peluchin.admin_service.service.RabbitAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints REST para administración de colas en RabbitMQ.
 * Cumple con el Indicador 6 y 8 de la evaluación.
 * 
 * Expone rutas compatibles tanto con /queues como con /api/v1/admin/queues.
 */
@RestController
@RequestMapping({"/queues", "/api/v1/admin/queues"})
public class QueueAdminController {

    private final RabbitAdminService rabbitAdminService;

    public QueueAdminController(RabbitAdminService rabbitAdminService) {
        this.rabbitAdminService = rabbitAdminService;
    }

    /**
     * Crea una nueva cola en RabbitMQ con validación de parámetros de entrada.
     * Ejemplo: POST /queues
     */
    @PostMapping
    public ResponseEntity<MensajeRespuesta> crearCola(@Valid @RequestBody CrearColaRequest request) {
        MensajeRespuesta respuesta = rabbitAdminService.crearCola(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /**
     * Consulta información de una cola existente.
     * Ejemplo: GET /queues/{name}
     */
    @GetMapping("/{name}")
    public ResponseEntity<QueueInfoResponse> obtenerCola(@PathVariable String name) {
        QueueInfoResponse info = rabbitAdminService.obtenerInformacionCola(name);
        return ResponseEntity.ok(info);
    }

    /**
     * Elimina una cola existente en RabbitMQ.
     * Ejemplo: DELETE /queues/{name}
     */
    @DeleteMapping("/{name}")
    public ResponseEntity<MensajeRespuesta> eliminarCola(@PathVariable String name) {
        boolean eliminada = rabbitAdminService.eliminarCola(name);
        if (eliminada) {
            return ResponseEntity.ok(MensajeRespuesta.exito("Cola '" + name + "' eliminada exitosamente", null));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(MensajeRespuesta.error(404, "La cola '" + name + "' no pudo ser eliminada o no existe", null));
        }
    }

    /**
     * Purga todos los mensajes de una cola.
     * Ejemplo: POST /queues/{name}/purge
     */
    @PostMapping("/{name}/purge")
    public ResponseEntity<MensajeRespuesta> purgarCola(@PathVariable String name) {
        rabbitAdminService.purgarCola(name);
        return ResponseEntity.ok(MensajeRespuesta.exito("Cola '" + name + "' purgada exitosamente", null));
    }
}
