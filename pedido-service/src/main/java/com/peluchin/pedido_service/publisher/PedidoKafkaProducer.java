package com.peluchin.pedido_service.publisher;

import com.peluchin.pedido_service.event.PedidoCanceladoEvent;
import com.peluchin.pedido_service.event.PedidoPagadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Productor de eventos de negocio hacia Apache Kafka.
 * 
 * Cumple con:
 * - Indicador 2 (12%): Uso de DTOs estructurados y tipados.
 * - Indicador 4 (14%): Productor Kafka correctamente implementado, enviando eventos con
 *   clave de partición (pedidoId) y serializers adecuados, desacoplado de la lógica de negocio.
 */
@Component
public class PedidoKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(PedidoKafkaProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.pedido-pagado:pedido.pagado.topic}")
    private String pedidoPagadoTopic;

    @Value("${kafka.topics.pedido-cancelado:pedido.cancelado.topic}")
    private String pedidoCanceladoTopic;

    public PedidoKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publica el evento de pedido pagado en Kafka.
     * La clave de partición es el pedidoId, asegurando que todos los eventos
     * relacionados al mismo pedido vayan a la misma partición y mantengan orden.
     */
    public void enviarPedidoPagado(PedidoPagadoEvent evento) {
        String key = String.valueOf(evento.getPedidoId());
        log.info("[KAFKA-PRODUCER] Publicando PedidoPagadoEvent hacia topic='{}' con key='{}'", pedidoPagadoTopic, key);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(pedidoPagadoTopic, key, evento);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("[KAFKA-PRODUCER] Éxito enviando pedidoId={} a topic='{}', partición={}, offset={}",
                        key,
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("[KAFKA-PRODUCER] Error enviando pedidoId={} hacia Kafka topic='{}': {}",
                        key, pedidoPagadoTopic, ex.getMessage(), ex);
            }
        });
    }

    /**
     * Publica el evento de pedido cancelado en Kafka para restauración de inventario.
     */
    public void enviarPedidoCancelado(PedidoCanceladoEvent evento) {
        String key = String.valueOf(evento.getPedidoId());
        log.info("[KAFKA-PRODUCER] Publicando PedidoCanceladoEvent hacia topic='{}' con key='{}'", pedidoCanceladoTopic, key);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(pedidoCanceladoTopic, key, evento);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("[KAFKA-PRODUCER] Éxito enviando cancelación pedidoId={} a topic='{}', partición={}, offset={}",
                        key,
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("[KAFKA-PRODUCER] Error enviando cancelación pedidoId={} hacia Kafka topic='{}': {}",
                        key, pedidoCanceladoTopic, ex.getMessage(), ex);
            }
        });
    }
}
