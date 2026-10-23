package com.peluchin.admin_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración centralizada de RabbitMQ para el microservicio administrador.
 * Cumple con los Indicadores 1, 2 y 3 de la evaluación.
 */
@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.name:peluchitos.exchange}")
    private String exchangeName;

    @Value("${rabbitmq.exchange.dlx:peluchitos.dlx}")
    private String dlxExchangeName;

    @Value("${rabbitmq.queues.pedido-pagado:pedido.pagado.queue}")
    private String pedidoPagadoQueueName;

    @Value("${rabbitmq.queues.pedido-pagado-dlq:pedido.pagado.dlq}")
    private String pedidoPagadoDlqName;

    @Value("${rabbitmq.routing-keys.pedido-pagado:pedido.pagado.key}")
    private String pedidoPagadoRoutingKey;

    @Value("${rabbitmq.routing-keys.pedido-pagado-dlq:pedido.pagado.dlq.key}")
    private String pedidoPagadoDlqRoutingKey;

    @Value("${rabbitmq.queues.pedido-cancelado:pedido.cancelado.queue}")
    private String pedidoCanceladoQueueName;

    @Value("${rabbitmq.queues.pedido-cancelado-dlq:pedido.cancelado.dlq}")
    private String pedidoCanceladoDlqName;

    @Value("${rabbitmq.routing-keys.pedido-cancelado:pedido.cancelado.key}")
    private String pedidoCanceladoRoutingKey;

    @Value("${rabbitmq.routing-keys.pedido-cancelado-dlq:pedido.cancelado.dlq.key}")
    private String pedidoCanceladoDlqRoutingKey;

    /**
     * Bean RabbitAdmin para operaciones administrativas sobre el broker.
     */
    @Bean
    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }

    /**
     * Serializador JSON de mensajes para comunicación estandarizada.
     */

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
    // =========================================================================
    // DECLARACIÓN DE BEANS: EXCHANGES (Principal y Dead Letter)
    // =========================================================================

    @Bean
    public TopicExchange mainExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(dlxExchangeName, true, false);
    }

    // =========================================================================
    // CASO DE USO 1: PEDIDO PAGADO (Actualización / Descuento de Stock)
    // =========================================================================

    @Bean
    public Queue pedidoPagadoQueue() {
        return QueueBuilder.durable(pedidoPagadoQueueName)
                .withArgument("x-dead-letter-exchange", dlxExchangeName)
                .withArgument("x-dead-letter-routing-key", pedidoPagadoDlqRoutingKey)
                .build();
    }

    @Bean
    public Queue pedidoPagadoDlq() {
        return QueueBuilder.durable(pedidoPagadoDlqName).build();
    }

    @Bean
    public Binding pedidoPagadoBinding() {
        return BindingBuilder.bind(pedidoPagadoQueue())
                .to(mainExchange())
                .with(pedidoPagadoRoutingKey);
    }

    @Bean
    public Binding pedidoPagadoDlqBinding() {
        return BindingBuilder.bind(pedidoPagadoDlq())
                .to(deadLetterExchange())
                .with(pedidoPagadoDlqRoutingKey);
    }

    // =========================================================================
    // CASO DE USO 2: PEDIDO CANCELADO (Restauración de Stock)
    // =========================================================================

    @Bean
    public Queue pedidoCanceladoQueue() {
        return QueueBuilder.durable(pedidoCanceladoQueueName)
                .withArgument("x-dead-letter-exchange", dlxExchangeName)
                .withArgument("x-dead-letter-routing-key", pedidoCanceladoDlqRoutingKey)
                .build();
    }

    @Bean
    public Queue pedidoCanceladoDlq() {
        return QueueBuilder.durable(pedidoCanceladoDlqName).build();
    }

    @Bean
    public Binding pedidoCanceladoBinding() {
        return BindingBuilder.bind(pedidoCanceladoQueue())
                .to(mainExchange())
                .with(pedidoCanceladoRoutingKey);
    }

    @Bean
    public Binding pedidoCanceladoDlqBinding() {
        return BindingBuilder.bind(pedidoCanceladoDlq())
                .to(deadLetterExchange())
                .with(pedidoCanceladoDlqRoutingKey);
    }
}
