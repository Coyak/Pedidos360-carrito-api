package com.pedidos360.carrito.messaging;

import com.pedidos360.carrito.dto.PedidoResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado de la publicación asíncrona de eventos de pedidos en RabbitMQ.
 */
@Service
public class PedidoEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PedidoEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${pedidos360.rabbitmq.exchange.pedidos}")
    private String exchange;

    @Value("${pedidos360.rabbitmq.routing-key.creado}")
    private String routingKey;

    public PedidoEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publica el evento de pedido creado hacia el TopicExchange con el payload serializado en JSON.
     *
     * @param pedido Datos del pedido confirmado
     */
    public void publicarPedidoCreado(PedidoResponseDTO pedido) {
        try {
            log.info("Publicando evento a RabbitMQ: pedidoId={}, exchange='{}', routingKey='{}'",
                    pedido.getPedidoId(), exchange, routingKey);
            rabbitTemplate.convertAndSend(exchange, routingKey, pedido);
            log.info("Evento publicado exitosamente para pedidoId={}", pedido.getPedidoId());
        } catch (Exception e) {
            log.error("Fallo crítico al publicar evento de pedidoId={} en RabbitMQ: {}",
                    pedido.getPedidoId(), e.getMessage(), e);
        }
    }
}
