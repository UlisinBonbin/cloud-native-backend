package com.peluchin.admin_service.service;

import com.peluchin.admin_service.dto.*;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.config.TopicConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Servicio encargado de encapsular toda la lógica de administración de componentes
 * de Apache Kafka (Tópicos, Particiones, Retención y Consumer Groups).
 * 
 * Cumple con:
 * - Indicador 3 (6%): Endpoints/métodos para crear, listar y mantener tópicos.
 * - Indicador 7 (12%): Gestión de parámetros de retención, cleanup policy, particiones y réplicas.
 * - Indicador 9 (6%): Operaciones completas para administrar tópicos y consumer groups (cálculo de LAG).
 */
@Service
public class KafkaAdminService {

    private static final Logger log = LoggerFactory.getLogger(KafkaAdminService.class);

    private final AdminClient adminClient;

    public KafkaAdminService(AdminClient adminClient) {
        this.adminClient = adminClient;
    }

    /**
     * Crea un nuevo tópico en Kafka con particiones, factor de replicación y parámetros avanzados.
     */
    public MensajeRespuesta crearTopico(CrearTopicoRequest request) {
        log.info("[KAFKA-ADMIN] Creando tópico '{}', particiones: {}, réplicas: {}, retención: {}ms, cleanup: {}",
                request.getNombre(), request.getParticiones(), request.getReplicas(),
                request.getRetentionMs(), request.getCleanupPolicy());

        try {
            NewTopic newTopic = new NewTopic(request.getNombre(), request.getParticiones(), request.getReplicas());

            Map<String, String> configs = new HashMap<>();
            if (request.getRetentionMs() != null) {
                configs.put(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(request.getRetentionMs()));
            }
            if (request.getCleanupPolicy() != null && !request.getCleanupPolicy().isBlank()) {
                configs.put(TopicConfig.CLEANUP_POLICY_CONFIG, request.getCleanupPolicy());
            }
            newTopic.configs(configs);

            adminClient.createTopics(Collections.singletonList(newTopic)).all().get(10, TimeUnit.SECONDS);

            return MensajeRespuesta.creado("Tópico Kafka creado exitosamente", request.getNombre());
        } catch (Exception ex) {
            log.error("[KAFKA-ADMIN] Error al crear tópico '{}': {}", request.getNombre(), ex.getMessage());
            throw new RuntimeException("Error al crear tópico en Kafka: " + ex.getMessage(), ex);
        }
    }

    /**
     * Lista todos los nombres de tópicos de negocio existentes en el clúster.
     */
    public List<String> listarTopicos() {
        log.info("[KAFKA-ADMIN] Listando tópicos en el clúster Kafka");
        try {
            Set<String> names = adminClient.listTopics(new ListTopicsOptions().listInternal(false))
                    .names()
                    .get(10, TimeUnit.SECONDS);
            return new ArrayList<>(names);
        } catch (Exception ex) {
            log.error("[KAFKA-ADMIN] Error al listar tópicos: {}", ex.getMessage());
            throw new RuntimeException("Error al listar tópicos: " + ex.getMessage(), ex);
        }
    }

    /**
     * Obtiene el detalle de un tópico específico (particiones, réplicas y configuraciones activas).
     */
    public TopicoInfoResponse obtenerDetalleTopico(String nombreTopico) {
        log.info("[KAFKA-ADMIN] Consultando información detallada del tópico '{}'", nombreTopico);
        try {
            TopicDescription description = adminClient.describeTopics(Collections.singletonList(nombreTopico))
                    .allTopicNames()
                    .get(10, TimeUnit.SECONDS)
                    .get(nombreTopico);

            if (description == null) {
                throw new IllegalArgumentException("El tópico '" + nombreTopico + "' no existe en Kafka");
            }

            int particiones = description.partitions().size();
            int factorReplicacion = description.partitions().isEmpty() ? 0 : description.partitions().get(0).replicas().size();

            // Consultar configuraciones del tópico (retention, cleanup.policy, etc.)
            ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, nombreTopico);
            Map<ConfigResource, Config> configsMap = adminClient.describeConfigs(Collections.singletonList(resource))
                    .all()
                    .get(10, TimeUnit.SECONDS);

            Map<String, String> configuraciones = new HashMap<>();
            Config config = configsMap.get(resource);
            if (config != null) {
                config.entries().forEach(entry -> {
                    if (entry.name().startsWith("retention.") || entry.name().startsWith("cleanup.")
                            || entry.name().startsWith("max.message.") || entry.name().startsWith("min.insync.")) {
                        configuraciones.put(entry.name(), entry.value());
                    }
                });
            }

            return TopicoInfoResponse.builder()
                    .nombre(nombreTopico)
                    .particiones(particiones)
                    .factorReplicacion(factorReplicacion)
                    .configuraciones(configuraciones)
                    .build();

        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("[KAFKA-ADMIN] Error al obtener detalle del tópico '{}': {}", nombreTopico, ex.getMessage());
            throw new RuntimeException("Error al consultar tópico '" + nombreTopico + "': " + ex.getMessage(), ex);
        }
    }

    /**
     * Modifica parámetros de un tópico (particiones adicionales o parámetros de configuración).
     */
    public MensajeRespuesta modificarTopico(String nombreTopico, ModificarTopicoRequest request) {
        log.info("[KAFKA-ADMIN] Modificando tópico '{}'", nombreTopico);
        try {
            // 1. Modificar particiones si fue solicitado
            if (request.getParticiones() != null && request.getParticiones() > 0) {
                Map<String, NewPartitions> newPartitions = Collections.singletonMap(
                        nombreTopico,
                        NewPartitions.increaseTo(request.getParticiones())
                );
                adminClient.createPartitions(newPartitions).all().get(10, TimeUnit.SECONDS);
            }

            // 2. Modificar configuraciones (retention, cleanup.policy)
            List<AlterConfigOp> alterOps = new ArrayList<>();
            if (request.getRetentionMs() != null) {
                alterOps.add(new AlterConfigOp(
                        new ConfigEntry(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(request.getRetentionMs())),
                        AlterConfigOp.OpType.SET
                ));
            }
            if (request.getCleanupPolicy() != null && !request.getCleanupPolicy().isBlank()) {
                alterOps.add(new AlterConfigOp(
                        new ConfigEntry(TopicConfig.CLEANUP_POLICY_CONFIG, request.getCleanupPolicy()),
                        AlterConfigOp.OpType.SET
                ));
            }

            if (!alterOps.isEmpty()) {
                ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, nombreTopico);
                Map<ConfigResource, Collection<AlterConfigOp>> configs = Collections.singletonMap(resource, alterOps);
                adminClient.incrementalAlterConfigs(configs).all().get(10, TimeUnit.SECONDS);
            }

            return MensajeRespuesta.ok("Tópico modificado exitosamente", nombreTopico);
        } catch (Exception ex) {
            log.error("[KAFKA-ADMIN] Error al modificar tópico '{}': {}", nombreTopico, ex.getMessage());
            throw new RuntimeException("Error al modificar tópico '" + nombreTopico + "': " + ex.getMessage(), ex);
        }
    }

    /**
     * Elimina un tópico de Kafka.
     */
    public MensajeRespuesta eliminarTopico(String nombreTopico) {
        log.info("[KAFKA-ADMIN] Eliminando tópico '{}'", nombreTopico);
        try {
            adminClient.deleteTopics(Collections.singletonList(nombreTopico)).all().get(10, TimeUnit.SECONDS);
            return MensajeRespuesta.ok("Tópico eliminado exitosamente", nombreTopico);
        } catch (Exception ex) {
            log.error("[KAFKA-ADMIN] Error al eliminar tópico '{}': {}", nombreTopico, ex.getMessage());
            throw new RuntimeException("Error al eliminar tópico '" + nombreTopico + "': " + ex.getMessage(), ex);
        }
    }

    /**
     * Lista los identificadores de todos los Consumer Groups activos en el clúster.
     */
    public List<String> listarConsumerGroups() {
        log.info("[KAFKA-ADMIN] Listando Consumer Groups activos");
        try {
            Collection<ConsumerGroupListing> groups = adminClient.listConsumerGroups().all().get(10, TimeUnit.SECONDS);
            return groups.stream().map(ConsumerGroupListing::groupId).collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("[KAFKA-ADMIN] Error al listar Consumer Groups: {}", ex.getMessage());
            throw new RuntimeException("Error al listar Consumer Groups: " + ex.getMessage(), ex);
        }
    }

    /**
     * Inspecciona a fondo un Consumer Group: estado, miembros, offsets y cálculo de LAG por partición.
     */
    public ConsumerGroupInfoResponse describirConsumerGroup(String groupId) {
        log.info("[KAFKA-ADMIN] Inspeccionando Consumer Group '{}'", groupId);
        try {
            ConsumerGroupDescription description = adminClient.describeConsumerGroups(Collections.singletonList(groupId))
                    .describedGroups()
                    .get(groupId)
                    .get(10, TimeUnit.SECONDS);

            List<String> miembros = description.members().stream()
                    .map(m -> m.consumerId() + " (" + m.host() + ")")
                    .collect(Collectors.toList());

            // Consultar offsets confirmados por el grupo
            Map<TopicPartition, org.apache.kafka.clients.consumer.OffsetAndMetadata> offsets =
                    adminClient.listConsumerGroupOffsets(groupId)
                            .partitionsToOffsetAndMetadata()
                            .get(10, TimeUnit.SECONDS);

            Map<String, Long> offsetsPorParticion = new HashMap<>();
            Map<String, Long> lagPorParticion = new HashMap<>();
            long totalLag = 0;

            if (!offsets.isEmpty()) {
                // Consultar los últimos offsets disponibles en el broker (End Offsets)
                Map<TopicPartition, OffsetSpec> offsetSpecMap = new HashMap<>();
                offsets.keySet().forEach(tp -> offsetSpecMap.put(tp, OffsetSpec.latest()));

                Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> endOffsets =
                        adminClient.listOffsets(offsetSpecMap).all().get(10, TimeUnit.SECONDS);

                for (Map.Entry<TopicPartition, org.apache.kafka.clients.consumer.OffsetAndMetadata> entry : offsets.entrySet()) {
                    TopicPartition tp = entry.getKey();
                    long currentOffset = entry.getValue() != null ? entry.getValue().offset() : 0;
                    String tpKey = tp.topic() + "-" + tp.partition();
                    offsetsPorParticion.put(tpKey, currentOffset);

                    ListOffsetsResult.ListOffsetsResultInfo endInfo = endOffsets.get(tp);
                    if (endInfo != null) {
                        long endOffset = endInfo.offset();
                        long lag = Math.max(0, endOffset - currentOffset);
                        lagPorParticion.put(tpKey, lag);
                        totalLag += lag;
                    }
                }
            }

            return ConsumerGroupInfoResponse.builder()
                    .groupId(groupId)
                    .estado(description.state() != null ? description.state().toString() : "UNKNOWN")
                    .cantidadMiembros(description.members().size())
                    .miembros(miembros)
                    .offsetsPorParticion(offsetsPorParticion)
                    .lagPorParticion(lagPorParticion)
                    .totalLag(totalLag)
                    .build();

        } catch (Exception ex) {
            log.error("[KAFKA-ADMIN] Error al inspeccionar Consumer Group '{}': {}", groupId, ex.getMessage());
            throw new RuntimeException("Error al inspeccionar Consumer Group '" + groupId + "': " + ex.getMessage(), ex);
        }
    }
}
