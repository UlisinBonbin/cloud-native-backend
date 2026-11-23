package com.peluchin.producto_service.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Consumidor de auditoría para Dead Letter Topics (DLT) de Kafka.
 * 
 * Cumple con:
 * - Indicador 6 (12%): Registro, monitoreo y auditoría de eventos fallidos o defectuosos
 *   que agotaron los reintentos en el consumidor principal, evitando bloqueos en el pipeline.
 */
@Component
public class InventarioKafkaDltConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventarioKafkaDltConsumer.class);

    @KafkaListener(
            topics = {
                    "${kafka.topics.pedido-pagado:pedido.pagado.topic}.DLT",
                    "${kafka.topics.pedido-cancelado:pedido.cancelado.topic}.DLT"
            },
            groupId = "inventario-dlt-audit-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void procesarMensajeFallidoDlt(
            ConsumerRecord<String, Object> record,
            @Header(value = KafkaHeaders.DLT_ORIGINAL_TOPIC, required = false) String originalTopic,
            @Header(value = KafkaHeaders.DLT_EXCEPTION_MESSAGE, required = false) String exceptionMessage,
            Acknowledgment acknowledgment) {

        log.warn("================== [ALERTA KAFKA DLT - EVENTO NO PROCESABLE] ==================");
        log.warn("Tópico DLT: {}, Partición: {}, Offset: {}, Clave: {}",
                record.topic(), record.partition(), record.offset(), record.key());
        log.warn("Tópico Original: {}", originalTopic);
        log.warn("Causa del Error: {}", exceptionMessage);
        log.warn("Payload recibido: {}", record.value());
        log.warn("===============================================================================");

        // Confirmamos lectura en la DLT para mantener el histórico de offsets al día
        acknowledgment.acknowledge();
    }
}
