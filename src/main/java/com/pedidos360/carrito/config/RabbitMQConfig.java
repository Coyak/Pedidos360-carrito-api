package com.pedidos360.carrito.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración centralizada de RabbitMQ para Pedidos360 Carrito API.
 * Define la topología de mensajería (Exchanges, Queues, DLQ y Bindings)
 * sin valores quemados en código y configura el conversor JSON con Jackson.
 */
@Configuration
public class RabbitMQConfig {

    @Value("${pedidos360.rabbitmq.exchange.pedidos}")
    private String pedidosExchangeName;

    @Value("${pedidos360.rabbitmq.exchange.dlx}")
    private String dlxExchangeName;

    @Value("${pedidos360.rabbitmq.queue.procesamiento}")
    private String procesamientoQueueName;

    @Value("${pedidos360.rabbitmq.queue.dlq}")
    private String dlqQueueName;

    @Value("${pedidos360.rabbitmq.routing-key.creado}")
    private String pedidoCreadoRoutingKey;

    @Value("${pedidos360.rabbitmq.routing-key.dlq}")
    private String dlqRoutingKey;

    // 1. Exchange Principal (TopicExchange para enrutamiento por tópicos de dominio)
    @Bean
    public TopicExchange pedidosExchange() {
        return new TopicExchange(pedidosExchangeName, true, false);
    }

    // 2. Exchange DLX (DirectExchange para direccionamiento directo a DLQ)
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(dlxExchangeName, true, false);
    }

    // 3. Cola Principal de Procesamiento configurada con Dead Letter Exchange
    @Bean
    public Queue procesamientoQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", dlxExchangeName);
        args.put("x-dead-letter-routing-key", dlqRoutingKey);

        return QueueBuilder.durable(procesamientoQueueName)
                .withArguments(args)
                .build();
    }

    // 4. Cola DLQ (Dead Letter Queue) para mensajes con fallo definitivo
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(dlqQueueName).build();
    }

    // 5. Enlace (Binding) entre Exchange Principal y Cola de Procesamiento
    @Bean
    public Binding bindingProcesamiento() {
        return BindingBuilder
                .bind(procesamientoQueue())
                .to(pedidosExchange())
                .with(pedidoCreadoRoutingKey);
    }

    // 6. Enlace (Binding) entre DLX y Cola DLQ
    @Bean
    public Binding bindingDeadLetter() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(dlqRoutingKey);
    }

    // 7. Conversor de mensajes a JSON (evita serialización binaria nativa)
    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    // 8. RabbitTemplate configurado con soporte JSON
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
