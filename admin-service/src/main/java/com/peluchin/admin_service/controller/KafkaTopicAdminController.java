package com.peluchin.admin_service.controller;

import com.peluchin.admin_service.dto.CrearTopicoRequest;
import com.peluchin.admin_service.dto.MensajeRespuesta;
import com.peluchin.admin_service.dto.ModificarTopicoRequest;
import com.peluchin.admin_service.dto.TopicoInfoResponse;
import com.peluchin.admin_service.service.KafkaAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para administración de Tópicos Kafka.
 * 
 * Cumple con:
 * - Indicador 3 (6%): Endpoints para crear, listar y mantener tópicos.
 * - Indicador 7 (12%): Parámetros avanzados de tópicos (retención, cleanup policy, particiones, réplicas).
 * - Indicador 9 (6%): Gestión de la plataforma Kafka vía API.
 */
@RestController
@RequestMapping("/kafka/topics")
public class KafkaTopicAdminController {

    private final KafkaAdminService kafkaAdminService;

    public KafkaTopicAdminController(KafkaAdminService kafkaAdminService) {
        this.kafkaAdminService = kafkaAdminService;
    }

    @PostMapping
    public ResponseEntity<MensajeRespuesta> crearTopico(@Valid @RequestBody CrearTopicoRequest request) {
        MensajeRespuesta respuesta = kafkaAdminService.crearTopico(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping
    public ResponseEntity<List<String>> listarTopicos() {
        return ResponseEntity.ok(kafkaAdminService.listarTopicos());
    }

    @GetMapping("/{nombre}")
    public ResponseEntity<TopicoInfoResponse> obtenerDetalleTopico(@PathVariable String nombre) {
        return ResponseEntity.ok(kafkaAdminService.obtenerDetalleTopico(nombre));
    }

    @PutMapping("/{nombre}")
    public ResponseEntity<MensajeRespuesta> modificarTopico(
            @PathVariable String nombre,
            @RequestBody ModificarTopicoRequest request) {
        return ResponseEntity.ok(kafkaAdminService.modificarTopico(nombre, request));
    }

    @DeleteMapping("/{nombre}")
    public ResponseEntity<MensajeRespuesta> eliminarTopico(@PathVariable String nombre) {
        return ResponseEntity.ok(kafkaAdminService.eliminarTopico(nombre));
    }
}
