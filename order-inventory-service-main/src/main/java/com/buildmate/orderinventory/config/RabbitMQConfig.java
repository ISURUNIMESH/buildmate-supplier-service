package com.buildmate.orderinventory.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.buildmate.orderinventory.events.MaterialEvent;
import com.buildmate.orderinventory.events.OrderCreatedEvent;
import com.buildmate.orderinventory.events.PaymentCompletedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * RabbitMQ topology for Order & Inventory.
 * Phase 1: publish OrderCreatedEvent (order.created).
 * Phase 2: consume PaymentCompletedEvent (payment.completed).
 * Phase 3: consume MaterialEvent (material.*) for inventory projection.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "buildmate.exchange";
    public static final String ORDER_CREATED_QUEUE = "order.created.queue";
    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";

    public static final String PAYMENT_COMPLETED_QUEUE = "payment.completed.queue";
    public static final String PAYMENT_COMPLETED_ROUTING_KEY = "payment.completed";

    public static final String MATERIAL_EVENTS_QUEUE = "order.inventory.material.events.queue";
    public static final String MATERIAL_CREATED_ROUTING_KEY = "material.created";
    public static final String MATERIAL_UPDATED_ROUTING_KEY = "material.updated";
    public static final String MATERIAL_STOCK_UPDATED_ROUTING_KEY = "material.stock.updated";
    public static final String MATERIAL_DELETED_ROUTING_KEY = "material.deleted";

    @Bean
    public TopicExchange buildmateExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return new Queue(ORDER_CREATED_QUEUE, true);
    }

    @Bean
    public Binding orderCreatedBinding(Queue orderCreatedQueue, TopicExchange buildmateExchange) {
        return BindingBuilder
                .bind(orderCreatedQueue)
                .to(buildmateExchange)
                .with(ORDER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Queue paymentCompletedQueue() {
        return new Queue(PAYMENT_COMPLETED_QUEUE, true);
    }

    @Bean
    public Binding paymentCompletedBinding(Queue paymentCompletedQueue, TopicExchange buildmateExchange) {
        return BindingBuilder
                .bind(paymentCompletedQueue)
                .to(buildmateExchange)
                .with(PAYMENT_COMPLETED_ROUTING_KEY);
    }

    @Bean
    public Queue materialEventsQueue() {
        return new Queue(MATERIAL_EVENTS_QUEUE, true);
    }

    @Bean
    public Binding materialCreatedBinding(Queue materialEventsQueue, TopicExchange buildmateExchange) {
        return BindingBuilder.bind(materialEventsQueue).to(buildmateExchange).with(MATERIAL_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding materialUpdatedBinding(Queue materialEventsQueue, TopicExchange buildmateExchange) {
        return BindingBuilder.bind(materialEventsQueue).to(buildmateExchange).with(MATERIAL_UPDATED_ROUTING_KEY);
    }

    @Bean
    public Binding materialStockUpdatedBinding(Queue materialEventsQueue, TopicExchange buildmateExchange) {
        return BindingBuilder.bind(materialEventsQueue).to(buildmateExchange).with(MATERIAL_STOCK_UPDATED_ROUTING_KEY);
    }

    @Bean
    public Binding materialDeletedBinding(Queue materialEventsQueue, TopicExchange buildmateExchange) {
        return BindingBuilder.bind(materialEventsQueue).to(buildmateExchange).with(MATERIAL_DELETED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jacksonJsonMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put(ORDER_CREATED_ROUTING_KEY, OrderCreatedEvent.class);
        idClassMapping.put(PAYMENT_COMPLETED_ROUTING_KEY, PaymentCompletedEvent.class);
        idClassMapping.put(MATERIAL_CREATED_ROUTING_KEY, MaterialEvent.class);
        idClassMapping.put(MATERIAL_UPDATED_ROUTING_KEY, MaterialEvent.class);
        idClassMapping.put(MATERIAL_STOCK_UPDATED_ROUTING_KEY, MaterialEvent.class);
        idClassMapping.put(MATERIAL_DELETED_ROUTING_KEY, MaterialEvent.class);
        typeMapper.setIdClassMapping(idClassMapping);
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.TYPE_ID);
        typeMapper.addTrustedPackages("*");
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter jacksonJsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jacksonJsonMessageConverter);
        rabbitTemplate.setExchange(EXCHANGE_NAME);
        return rabbitTemplate;
    }
}
