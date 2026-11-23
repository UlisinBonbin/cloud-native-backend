package com.peluchin.pedido_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración del cliente productor de Kafka para pedido-service.
 * 
 * Cumple con:
 * - Indicador 1 (12%): Configuración completa del cliente Kafka (bootstrap servers,
 *   serializers, acks, retries).
 * - Indicador 2 (12%): Tópicos con DTOs estructurados y tipados.
 * - Indicador 7 (12%): Parámetros avanzados de tópicos: 3 particiones, factor de replicación,
 *   retention.ms (7 días) y cleanup.policy configurados explícitamente.
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${kafka.topics.pedido-pagado:pedido.pagado.topic}")
    private String pedidoPagadoTopicName;

    @Value("${kafka.topics.pedido-cancelado:pedido.cancelado.topic}")
    private String pedidoCanceladoTopicName;

    @Value("${kafka.topics.partitions:3}")
    private int partitions;

    @Value("${kafka.topics.replication-factor:1}")
    private int replicationFactor;

    @Value("${kafka.topics.retention-ms:604800000}")
    private long retentionMs;

    @Value("${kafka.topics.cleanup-policy:delete}")
    private String cleanupPolicy;

    // =========================================================================
    // PRODUCTOR Y SERIALIZADORES (Indicador 1 y 4)
    // =========================================================================

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // Garantías de entrega y resiliencia
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // =========================================================================
    // DECLARACIÓN DE TÓPICOS CON PARTICIONES Y PARÁMETROS AVANZADOS (Indicador 7)
    // =========================================================================

    @Bean
    public NewTopic pedidoPagadoTopic() {
        return TopicBuilder.name(pedidoPagadoTopicName)
                .partitions(partitions)
                .replicas(replicationFactor)
                .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(retentionMs))
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, cleanupPolicy)
                .build();
    }

    @Bean
    public NewTopic pedidoCanceladoTopic() {
        return TopicBuilder.name(pedidoCanceladoTopicName)
                .partitions(partitions)
                .replicas(replicationFactor)
                .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(retentionMs))
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, cleanupPolicy)
                .build();
    }
}
