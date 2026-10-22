package com.peluchin.pedido_service.publisher;

import com.peluchin.pedido_service.event.PedidoCanceladoEvent;
import com.peluchin.pedido_service.event.PedidoPagadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Publicador de eventos de negocio para el microservicio de pedidos.
 * Aísla completamente la infraestructura de RabbitMQ de la lógica de negocio (Indicador 3).
 */
@Component
public class PedidoEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PedidoEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name:peluchitos.exchange}")
    private String exchangeName;

    @Value("${rabbitmq.routing-keys.pedido-pagado:pedido.pagado.key}")
    private String pedidoPagadoRoutingKey;

    @Value("${rabbitmq.routing-keys.pedido-cancelado:pedido.cancelado.key}")
    private String pedidoCanceladoRoutingKey;

    public PedidoEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publica el evento de pedido pagado para que el servicio de inventario/producto
     * descuente y actualice el stock de forma asíncrona.
     */
    public void publicarPedidoPagado(PedidoPagadoEvent evento) {
        log.info("Publicando evento PedidoPagadoEvent para pedidoId={} hacia exchange='{}' con routingKey='{}'",
                evento.getPedidoId(), exchangeName, pedidoPagadoRoutingKey);
        rabbitTemplate.convertAndSend(exchangeName, pedidoPagadoRoutingKey, evento);
    }

    /**
     * Publica el evento de pedido cancelado para que el servicio de inventario/producto
     * reponga el stock de forma asíncrona.
     */
    public void publicarPedidoCancelado(PedidoCanceladoEvent evento) {
        log.info("Publicando evento PedidoCanceladoEvent para pedidoId={} hacia exchange='{}' con routingKey='{}'",
                evento.getPedidoId(), exchangeName, pedidoCanceladoRoutingKey);
        rabbitTemplate.convertAndSend(exchangeName, pedidoCanceladoRoutingKey, evento);
    }
}
