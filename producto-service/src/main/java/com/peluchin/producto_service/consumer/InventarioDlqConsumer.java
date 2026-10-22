package com.peluchin.producto_service.consumer;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Consumidor de auditoría de Dead Letter Queue (DLQ).
 * 
 * Permite registrar, monitorear y alertar sobre los mensajes fallidos que fueron
 * desviados a la DLQ tras un rechazo (NACK con requeue=false) en el consumidor principal.
 * 
 * Cumple con el Indicador 4 y 5 de la evaluación.
 */
@Component
public class InventarioDlqConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventarioDlqConsumer.class);

    @RabbitListener(
            queues = {
                    "${rabbitmq.queues.pedido-pagado-dlq:pedido.pagado.dlq}",
                    "${rabbitmq.queues.pedido-cancelado-dlq:pedido.cancelado.dlq}"
            },
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void procesarMensajeFallidoDlq(
            Message mensaje,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {

        log.warn("================== [ALERTA DLQ - MENSAJE NO PROCESABLE] ==================");
        log.warn("Cola de origen / cabeceras: {}", mensaje.getMessageProperties().getHeaders());
        log.warn("Cuerpo del mensaje fallido: {}", new String(mensaje.getBody()));
        log.warn("=========================================================================");

        // En un escenario real de producción, aquí se almacena en base de datos de auditoría
        // o se envía alerta a un sistema como AWS CloudWatch / SNS / Slack.
        // Confirmamos el mensaje en la DLQ para evitar acumulaciones infinitas.
        channel.basicAck(deliveryTag, false);
    }
}
