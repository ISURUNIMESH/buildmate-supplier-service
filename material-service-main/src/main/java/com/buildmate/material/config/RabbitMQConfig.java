package com.buildmate.material.config;

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

import com.buildmate.material.events.MaterialEvent;
import com.buildmate.material.events.SupplierDeletedEvent;
import com.buildmate.material.events.SupplierEvent;
import com.buildmate.material.events.SupplierStatusChangedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * RabbitMQ topology for Material Service.
 * Publishes material.* events and consumes supplier.* events on buildmate.exchange.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "buildmate.exchange";

    public static final String MATERIAL_CREATED_ROUTING_KEY = "material.created";
    public static final String MATERIAL_UPDATED_ROUTING_KEY = "material.updated";
    public static final String MATERIAL_STOCK_UPDATED_ROUTING_KEY = "material.stock.updated";
    public static final String MATERIAL_DELETED_ROUTING_KEY = "material.deleted";

    public static final String MATERIAL_SUPPLIER_EVENTS_QUEUE = "material.supplier.events.queue";

    public static final String SUPPLIER_CREATED_ROUTING_KEY = "supplier.created";
    public static final String SUPPLIER_UPDATED_ROUTING_KEY = "supplier.updated";
    public static final String SUPPLIER_STATUS_CHANGED_ROUTING_KEY = "supplier.status.changed";
    public static final String SUPPLIER_DELETED_ROUTING_KEY = "supplier.deleted";

    @Bean
    public TopicExchange buildmateExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue materialSupplierEventsQueue() {
        return new Queue(MATERIAL_SUPPLIER_EVENTS_QUEUE, true);
    }

    @Bean
    public Binding supplierCreatedBinding(Queue materialSupplierEventsQueue, TopicExchange buildmateExchange) {
        return BindingBuilder.bind(materialSupplierEventsQueue)
                .to(buildmateExchange)
                .with(SUPPLIER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding supplierUpdatedBinding(Queue materialSupplierEventsQueue, TopicExchange buildmateExchange) {
        return BindingBuilder.bind(materialSupplierEventsQueue)
                .to(buildmateExchange)
                .with(SUPPLIER_UPDATED_ROUTING_KEY);
    }

    @Bean
    public Binding supplierStatusChangedBinding(Queue materialSupplierEventsQueue, TopicExchange buildmateExchange) {
        return BindingBuilder.bind(materialSupplierEventsQueue)
                .to(buildmateExchange)
                .with(SUPPLIER_STATUS_CHANGED_ROUTING_KEY);
    }

    @Bean
    public Binding supplierDeletedBinding(Queue materialSupplierEventsQueue, TopicExchange buildmateExchange) {
        return BindingBuilder.bind(materialSupplierEventsQueue)
                .to(buildmateExchange)
                .with(SUPPLIER_DELETED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jacksonJsonMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put(MATERIAL_CREATED_ROUTING_KEY, MaterialEvent.class);
        idClassMapping.put(MATERIAL_UPDATED_ROUTING_KEY, MaterialEvent.class);
        idClassMapping.put(MATERIAL_STOCK_UPDATED_ROUTING_KEY, MaterialEvent.class);
        idClassMapping.put(MATERIAL_DELETED_ROUTING_KEY, MaterialEvent.class);
        idClassMapping.put(SUPPLIER_CREATED_ROUTING_KEY, SupplierEvent.class);
        idClassMapping.put(SUPPLIER_UPDATED_ROUTING_KEY, SupplierEvent.class);
        idClassMapping.put(SUPPLIER_STATUS_CHANGED_ROUTING_KEY, SupplierStatusChangedEvent.class);
        idClassMapping.put(SUPPLIER_DELETED_ROUTING_KEY, SupplierDeletedEvent.class);
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
