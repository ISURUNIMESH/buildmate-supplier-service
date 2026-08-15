package com.buildmate.supplier.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.buildmate.supplier.events.SupplierDeletedEvent;
import com.buildmate.supplier.events.SupplierEvent;
import com.buildmate.supplier.events.SupplierStatusChangedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Supplier publishes domain events to the shared {@code buildmate.exchange}.
 * Does not redefine Order/Payment queues or routing keys.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "buildmate.exchange";

    public static final String SUPPLIER_CREATED_ROUTING_KEY = "supplier.created";
    public static final String SUPPLIER_UPDATED_ROUTING_KEY = "supplier.updated";
    public static final String SUPPLIER_STATUS_CHANGED_ROUTING_KEY = "supplier.status.changed";
    public static final String SUPPLIER_DELETED_ROUTING_KEY = "supplier.deleted";

    @Bean
    public TopicExchange buildmateExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public MessageConverter jacksonJsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        Map<String, Class<?>> idClassMapping = new HashMap<>();
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
