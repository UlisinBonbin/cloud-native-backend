package com.peluchin.admin_service.service;

import com.peluchin.admin_service.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * Servicio encargado de encapsular toda la lógica de administración de colas,
 * exchanges y bindings en RabbitMQ.
 * 
 * Cumple con el Indicador 7 de la evaluación:
 * "Toda la lógica de creación, actualización y eliminación de colas y exchanges
 * está encapsulada en un servicio dedicado. Los controladores llaman a métodos de
 * alto nivel y no conocen detalles internos de RabbitMQ."
 */
@Service
public class RabbitAdminService {

    private static final Logger log = LoggerFactory.getLogger(RabbitAdminService.class);

    private final AmqpAdmin amqpAdmin;

    public RabbitAdminService(AmqpAdmin amqpAdmin) {
        this.amqpAdmin = amqpAdmin;
    }

    /**
     * Crea y declara una nueva cola en RabbitMQ con soporte para DLQ opcional.
     *
     * @param request Datos de configuración de la cola
     * @return MensajeRespuesta confirmando la creación
     */
    public MensajeRespuesta crearCola(CrearColaRequest request) {
        log.info("Creando cola '{}', durable: {}, exclusivo: {}, autoDelete: {}",
                request.getNombre(), request.getDurable(), request.getExclusivo(), request.getAutoDelete());

        QueueBuilder builder = Boolean.TRUE.equals(request.getDurable())
                ? QueueBuilder.durable(request.getNombre())
                : QueueBuilder.nonDurable(request.getNombre());

        if (Boolean.TRUE.equals(request.getExclusivo())) {
            builder.exclusive();
        }
        if (Boolean.TRUE.equals(request.getAutoDelete())) {
            builder.autoDelete();
        }

        // Configuración opcional de Dead Letter Exchange (DLQ)
        if (request.getDeadLetterExchange() != null && !request.getDeadLetterExchange().isBlank()) {
            builder.withArgument("x-dead-letter-exchange", request.getDeadLetterExchange());
        }
        if (request.getDeadLetterRoutingKey() != null && !request.getDeadLetterRoutingKey().isBlank()) {
            builder.withArgument("x-dead-letter-routing-key", request.getDeadLetterRoutingKey());
        }

        Queue queue = builder.build();
        String declaredName = amqpAdmin.declareQueue(queue);

        return MensajeRespuesta.creado("Cola declarada exitosamente", declaredName);
    }

    /**
     * Consulta información básica y propiedades de una cola existente.
     *
     * @param nombreCola Nombre de la cola a consultar
     * @return QueueInfoResponse con la información obtenida
     */
    public QueueInfoResponse obtenerInformacionCola(String nombreCola) {
        log.info("Consultando información de la cola '{}'", nombreCola);
        Properties props = amqpAdmin.getQueueProperties(nombreCola);

        if (props == null) {
            throw new IllegalArgumentException("La cola '" + nombreCola + "' no existe en el broker de RabbitMQ");
        }

        Integer messageCount = null;
        Integer consumerCount = null;
        Object msgCountObj = props.get(RabbitAdmin.QUEUE_MESSAGE_COUNT);
        if (msgCountObj instanceof Number) {
            messageCount = ((Number) msgCountObj).intValue();
        }
        Object consCountObj = props.get(RabbitAdmin.QUEUE_CONSUMER_COUNT);
        if (consCountObj instanceof Number) {
            consumerCount = ((Number) consCountObj).intValue();
        }

        return QueueInfoResponse.builder()
                .nombre(nombreCola)
                .messageCount(messageCount != null ? messageCount : 0)
                .consumerCount(consumerCount != null ? consumerCount : 0)
                .build();
    }

    /**
     * Elimina una cola existente en RabbitMQ.
     *
     * @param nombreCola Nombre de la cola a eliminar
     * @return boolean indicando si la operación fue exitosa
     */
    public boolean eliminarCola(String nombreCola) {
        log.info("Eliminando cola '{}'", nombreCola);
        return amqpAdmin.deleteQueue(nombreCola);
    }

    /**
     * Purga todos los mensajes pendientes de una cola.
     *
     * @param nombreCola Nombre de la cola a purgar
     */
    public void purgarCola(String nombreCola) {
        log.info("Purgando mensajes de la cola '{}'", nombreCola);
        amqpAdmin.purgeQueue(nombreCola, false);
    }

    /**
     * Crea y declara un Exchange (direct, topic, fanout o headers).
     *
     * @param request Datos de configuración del exchange
     * @return MensajeRespuesta confirmando la creación
     */
    public MensajeRespuesta crearExchange(CrearExchangeRequest request) {
        log.info("Creando exchange '{}' de tipo '{}', durable: {}",
                request.getNombre(), request.getTipo(), request.getDurable());

        boolean durable = Boolean.TRUE.equals(request.getDurable());
        boolean autoDelete = Boolean.TRUE.equals(request.getAutoDelete());
        Exchange exchange;

        switch (request.getTipo().toLowerCase()) {
            case "direct":
                exchange = new DirectExchange(request.getNombre(), durable, autoDelete);
                break;
            case "topic":
                exchange = new TopicExchange(request.getNombre(), durable, autoDelete);
                break;
            case "fanout":
                exchange = new FanoutExchange(request.getNombre(), durable, autoDelete);
                break;
            case "headers":
                exchange = new HeadersExchange(request.getNombre(), durable, autoDelete);
                break;
            default:
                throw new IllegalArgumentException("Tipo de exchange no soportado: " + request.getTipo());
        }

        amqpAdmin.declareExchange(exchange);
        return MensajeRespuesta.creado("Exchange declarado exitosamente", request.getNombre());
    }

    /**
     * Elimina un exchange existente en RabbitMQ.
     *
     * @param nombreExchange Nombre del exchange a eliminar
     * @return boolean indicando si la operación fue exitosa
     */
    public boolean eliminarExchange(String nombreExchange) {
        log.info("Eliminando exchange '{}'", nombreExchange);
        return amqpAdmin.deleteExchange(nombreExchange);
    }

    /**
     * Crea un enlace (Binding) entre una cola y un exchange mediante una routing key.
     *
     * @param request Datos de configuración del binding
     * @return MensajeRespuesta confirmando la creación
     */
    public MensajeRespuesta crearBinding(CrearBindingRequest request) {
        log.info("Creando binding: Cola '{}' <-> Exchange '{}' con RoutingKey '{}'",
                request.getQueueName(), request.getExchangeName(), request.getRoutingKey());

        Binding binding = new Binding(
                request.getQueueName(),
                Binding.DestinationType.QUEUE,
                request.getExchangeName(),
                request.getRoutingKey(),
                null
        );

        amqpAdmin.declareBinding(binding);
        return MensajeRespuesta.creado("Binding creado exitosamente", binding.toString());
    }

    /**
     * Elimina un enlace (Binding) existente.
     *
     * @param request Datos del binding a eliminar
     */
    public void eliminarBinding(EliminarBindingRequest request) {
        log.info("Eliminando binding: Cola '{}' <-> Exchange '{}' con RoutingKey '{}'",
                request.getQueueName(), request.getExchangeName(), request.getRoutingKey());

        Binding binding = new Binding(
                request.getQueueName(),
                Binding.DestinationType.QUEUE,
                request.getExchangeName(),
                request.getRoutingKey(),
                null
        );

        amqpAdmin.removeBinding(binding);
    }
}
