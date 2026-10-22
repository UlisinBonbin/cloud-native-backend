package com.peluchin.producto_service.consumer;

import com.peluchin.producto_service.event.PedidoCanceladoEvent;
import com.peluchin.producto_service.event.PedidoItemEvent;
import com.peluchin.producto_service.event.PedidoPagadoEvent;
import com.peluchin.producto_service.service.ProductoService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Consumidor de eventos perteneciente al dominio funcional de INVENTARIO Y STOCK.
 * 
 * Cumple con:
 * - Indicador 4 (15%): Implementación con @RabbitListener organizada claramente por dominio funcional.
 * - Indicador 5 (20%): Confirmación explícita de mensajes (ACK manual) y manejo estructurado de errores:
 *     - channel.basicAck() cuando el procesamiento es exitoso.
 *     - channel.basicNack(deliveryTag, false, false) con requeue=false para desviar a DLQ (Dead Letter Queue)
 *       ante errores no recuperables o fallos de negocio (evitando ciclos infinitos de requeue).
 */
@Component
public class InventarioConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventarioConsumer.class);

    private final ProductoService productoService;

    public InventarioConsumer(ProductoService productoService) {
        this.productoService = productoService;
    }

    /**
     * Consumidor del caso de uso 1: Pedido Pagado.
     * Escucha la cola de pedidos pagados para descontar el stock de los productos.
     * 
     * Estrategia de Confirmación y Manejo de Errores (Indicador 5):
     * 1. Éxito: Se ejecuta channel.basicAck confirmando que el inventario fue actualizado.
     * 2. Error de Negocio / Validación (ej. producto inexistente, payload corrupto):
     *    No es recuperable de inmediato, por lo que se envía NACK con requeue=false.
     *    RabbitMQ traslada el mensaje a la Dead Letter Queue (DLQ) configurada mediante DLX.
     * 3. Error general o inesperado: Se documenta y se envía a DLQ para intervención y análisis.
     */
    @RabbitListener(
            queues = "${rabbitmq.queues.pedido-pagado:pedido.pagado.queue}",
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void consumirPedidoPagado(
            @Payload PedidoPagadoEvent evento,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {

        log.info("[INVENTARIO-CONSUMER] Mensaje recibido para pedidoId={}. DeliveryTag={}",
                evento != null ? evento.getPedidoId() : "NULL", deliveryTag);

        try {
            // Validación de integridad del mensaje recibido
            if (evento == null || evento.getItems() == null || evento.getItems().isEmpty()) {
                log.error("[INVENTARIO-CONSUMER] Error de validación: Mensaje corrupto o sin items. Enviando a DLQ.");
                // NACK con requeue = false -> RabbitMQ enruta a 'pedido.pagado.dlq'
                channel.basicNack(deliveryTag, false, false);
                return;
            }

            // Procesamiento de negocio: Descontar stock para cada producto del pedido
            for (PedidoItemEvent item : evento.getItems()) {
                log.info("[INVENTARIO-CONSUMER] Descontando stock: ProductoId={}, Cantidad={}",
                        item.getProductoId(), item.getCantidad());
                productoService.descontarStock(item.getProductoId(), item.getCantidad());
            }

            // Confirmación explícita (ACK) tras procesamiento exitoso
            channel.basicAck(deliveryTag, false);
            log.info("[INVENTARIO-CONSUMER] ACK enviado exitosamente para pedidoId={}", evento.getPedidoId());

        } catch (IllegalArgumentException ex) {
            // Error de validación de entrada (parámetros inválidos)
            log.error("[INVENTARIO-CONSUMER] Error de validación en parámetros de pedidoId={}: {}. Enviando a DLQ.",
                    evento != null ? evento.getPedidoId() : "desconocido", ex.getMessage());
            // Envío explícito a DLQ (requeue = false)
            channel.basicNack(deliveryTag, false, false);

        } catch (RuntimeException ex) {
            // Error de negocio en tiempo de ejecución (stock insuficiente o producto no encontrado)
            log.error("[INVENTARIO-CONSUMER] Error de negocio no recuperable para pedidoId={}: {}. Enviando a DLQ.",
                    evento != null ? evento.getPedidoId() : "desconocido", ex.getMessage());
            // Envío explícito a DLQ (requeue = false)
            channel.basicNack(deliveryTag, false, false);

        } catch (Exception ex) {
            // Error imprevisto o fallo del sistema
            log.error("[INVENTARIO-CONSUMER] Error crítico inesperado al procesar deliveryTag={}: {}. Desviando a DLQ.",
                    deliveryTag, ex.getMessage(), ex);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    /**
     * Consumidor del caso de uso 2: Pedido Cancelado.
     * Escucha la cola de pedidos cancelados para restaurar/reponer el stock en inventario.
     */
    @RabbitListener(
            queues = "${rabbitmq.queues.pedido-cancelado:pedido.cancelado.queue}",
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void consumirPedidoCancelado(
            @Payload PedidoCanceladoEvent evento,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {

        log.info("[INVENTARIO-CONSUMER] Evento Pedido Cancelado recibido para pedidoId={}. DeliveryTag={}",
                evento != null ? evento.getPedidoId() : "NULL", deliveryTag);

        try {
            if (evento == null || evento.getItems() == null || evento.getItems().isEmpty()) {
                log.warn("[INVENTARIO-CONSUMER] Pedido cancelado sin items. Enviando a DLQ.");
                channel.basicNack(deliveryTag, false, false);
                return;
            }

            // Restaurar stock de los productos cancelados
            for (PedidoItemEvent item : evento.getItems()) {
                log.info("[INVENTARIO-CONSUMER] Restaurando stock: ProductoId={}, Cantidad={}",
                        item.getProductoId(), item.getCantidad());
                productoService.reponerStock(item.getProductoId(), item.getCantidad());
            }

            // ACK explícito
            channel.basicAck(deliveryTag, false);
            log.info("[INVENTARIO-CONSUMER] Stock restaurado y ACK enviado para pedidoId={}", evento.getPedidoId());

        } catch (Exception ex) {
            log.error("[INVENTARIO-CONSUMER] Error al restaurar stock de pedido cancelado {}: {}. Enviando a DLQ.",
                    evento != null ? evento.getPedidoId() : "desconocido", ex.getMessage());
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
