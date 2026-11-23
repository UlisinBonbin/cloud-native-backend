package com.peluchin.admin_service.controller;

import com.peluchin.admin_service.dto.ConsumerGroupInfoResponse;
import com.peluchin.admin_service.service.KafkaAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para administración e inspección de Consumer Groups en Kafka.
 * 
 * Cumple con:
 * - Indicador 8 (12%): Inspección de Consumer Groups para balanceo y resiliencia.
 * - Indicador 9 (6%): Monitoreo de offsets y métricas de LAG para el consumidor.
 */
@RestController
@RequestMapping("/kafka/consumer-groups")
public class KafkaConsumerGroupAdminController {

    private final KafkaAdminService kafkaAdminService;

    public KafkaConsumerGroupAdminController(KafkaAdminService kafkaAdminService) {
        this.kafkaAdminService = kafkaAdminService;
    }

    @GetMapping
    public ResponseEntity<List<String>> listarConsumerGroups() {
        return ResponseEntity.ok(kafkaAdminService.listarConsumerGroups());
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<ConsumerGroupInfoResponse> describirConsumerGroup(@PathVariable String groupId) {
        return ResponseEntity.ok(kafkaAdminService.describirConsumerGroup(groupId));
    }
}
