package com.example.notification.config;

import com.example.notification.model.NotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${kafka.consumer.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${kafka.consumer.retry.backoff-initial:1000}")
    private long backoffInitialInterval;

    @Value("${kafka.consumer.retry.backoff-multiplier:2.0}")
    private double backoffMultiplier;

    @Value("${kafka.consumer.retry.backoff-max:10000}")
    private long backoffMaxInterval;

    @Bean
    public ConsumerFactory<String, NotificationEvent> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        
        // Wrap JsonDeserializer with ErrorHandlingDeserializer for robustness
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, NotificationEvent.class.getName());
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        
        // Consumer behavior
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Manual commit for better control
        
        // Performance tuning
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        configProps.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1);
        configProps.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(consumerFactory());
        
        // Enable batch processing for better throughput
        factory.setBatchListener(false);
        
        // Set concurrency for parallel processing
        factory.setConcurrency(3);
        
        // Enable manual acknowledgment for better control
        factory.getContainerProperties().setAckMode(
            org.springframework.kafka.listener.ContainerProperties.AckMode.RECORD
        );
        
        // Set common error handler with exponential backoff
        factory.setCommonErrorHandler(errorHandler());
        
        return factory;
    }

    @Bean
    public CommonErrorHandler errorHandler() {
        // Configure exponential backoff for retries
        ExponentialBackOff backOff = new ExponentialBackOff();
        backOff.setInitialInterval(backoffInitialInterval);
        backOff.setMultiplier(backoffMultiplier);
        backOff.setMaxInterval(backoffMaxInterval);
        backOff.setMaxElapsedTime(backoffMaxInterval * maxRetryAttempts);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler((consumerRecord, exception) -> {
            // This is called when all retries are exhausted
            log.error("Failed to process record after {} attempts. Record: {}, Error: {}", 
                maxRetryAttempts, consumerRecord.value(), exception.getMessage());
            // DLQ logic will be handled in the DLQ producer
        }, backOff);

        // Don't retry for certain exceptions
        errorHandler.addNotRetryableExceptions(
            IllegalArgumentException.class,
            org.springframework.kafka.support.serializer.DeserializationException.class
        );

        return errorHandler;
    }
}
