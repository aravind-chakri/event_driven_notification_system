package com.example.notification.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.topic.notification}")
    private String notificationTopic;

    @Value("${kafka.topic.dlq}")
    private String dlqTopic;

    @Value("${kafka.topic.notification.partitions:3}")
    private int partitions;

    @Value("${kafka.topic.notification.replicas:1}")
    private int replicas;

    @Bean
    public NewTopic notificationTopic() {
        return TopicBuilder
                .name(notificationTopic)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

    @Bean
    public NewTopic deadLetterQueueTopic() {
        return TopicBuilder
                .name(dlqTopic)
                .partitions(1) // Single partition for DLQ to maintain order
                .replicas(replicas)
                .build();
    }
}
