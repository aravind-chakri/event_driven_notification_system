# Event-Driven Notification System

A scalable, event-driven notification system built with **Apache Kafka** and **Spring Boot** that supports asynchronous delivery of email and SMS notifications. This system demonstrates microservices architecture principles with loose coupling and high scalability.

## Architecture Overview

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│   Client    │────────▶│  REST API   │────────▶│   Kafka     │
│ Application │         │ Controller  │         │  Producer   │
└─────────────┘         └─────────────┘         └──────┬──────┘
                                                        │
                                                        ▼
                                                ┌───────────────┐
                                                │ Kafka Cluster │
                                                │  (Topic: notifications)
                                                └───────┬───────┘
                                                        │
                                                        ▼
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│   Email     │◀────────│   Kafka     │◀────────│ Notification│
│   Service   │         │  Consumer   │         │  Processor  │
└─────────────┘         └─────────────┘         └─────────────┘
                                │
                                ▼
                        ┌─────────────┐
                        │    SMS      │
                        │   Service   │
                        └─────────────┘
```

## Key Features

- **Event-Driven Architecture**: Leverages Apache Kafka for asynchronous message processing
- **Decoupled Services**: Producer and consumer services are completely independent
- **Multi-Channel Support**: Supports both Email and SMS notifications
- **Scalability**: Easily scale consumers independently based on load
- **Fault Tolerance**: Built-in retry mechanism and dead letter queue support
- **RESTful API**: Simple HTTP interface for triggering notifications
- **Production-Ready**: Includes idempotent producers, proper error handling, and logging

## Technology Stack

- **Java 21**
- **Spring Boot 4.0.5**
- **Apache Kafka** - Message broker for event streaming
- **Spring Kafka** - Kafka integration
- **Spring Mail** - Email delivery
- **Maven** - Dependency management
- **Lombok** - Reduce boilerplate code
- **Docker Compose** - Local development environment

## Project Structure

```
notification/
├── src/
│   ├── main/
│   │   ├── java/com/example/notification/
│   │   │   ├── config/
│   │   │   │   └── KafkaTopicConfig.java          # Kafka topic configuration
│   │   │   ├── controller/
│   │   │   │   └── NotificationController.java    # REST API endpoints
│   │   │   ├── dto/
│   │   │   │   ├── NotificationRequest.java       # API request model
│   │   │   │   └── NotificationResponse.java      # API response model
│   │   │   ├── model/
│   │   │   │   ├── NotificationEvent.java         # Core domain model
│   │   │   │   ├── NotificationType.java          # Email/SMS enum
│   │   │   │   └── NotificationStatus.java        # Status tracking
│   │   │   ├── service/
│   │   │   │   ├── NotificationProducer.java      # Kafka producer
│   │   │   │   ├── NotificationConsumer.java      # Kafka consumer
│   │   │   │   ├── NotificationService.java       # Service interface
│   │   │   │   ├── EmailNotificationService.java  # Email implementation
│   │   │   │   ├── SmsNotificationService.java    # SMS implementation
│   │   │   │   └── NotificationOrchestrationService.java
│   │   │   └── NotificationApplication.java       # Main application
│   │   └── resources/
│   │       ├── application.properties              # Application config
│   │       └── application-dev.properties          # Dev environment config
│   └── test/
├── docker-compose.yml                               # Local Kafka setup
├── pom.xml                                          # Maven dependencies
└── README.md                                        # This file
```

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.6+
- Docker and Docker Compose (for running Kafka locally)

### Running the Application

#### 1. Start Kafka and Zookeeper

```bash
docker-compose up -d
```

This will start:
- Zookeeper on port 2181
- Kafka broker on port 9092
- Kafka UI on port 8090 (optional, for monitoring)

#### 2. Build the application

```bash
./mvnw clean install
```

#### 3. Run the application

```bash
./mvnw spring-boot:run
```

Or with development profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The application will start on `http://localhost:8080`

## API Documentation

### Send Notification

**Endpoint:** `POST /api/v1/notifications`

**Request Body:**

```json
{
  "type": "EMAIL",
  "recipient": "user@example.com",
  "subject": "Welcome to our platform",
  "message": "Thank you for signing up!",
  "metadata": {
    "userId": "12345",
    "campaign": "onboarding"
  }
}
```

**Response:**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PENDING",
  "message": "Notification queued for processing",
  "timestamp": "2026-04-06T10:30:00"
}
```

**Status Codes:**
- `202 Accepted` - Notification queued successfully
- `400 Bad Request` - Invalid request payload
- `500 Internal Server Error` - Server error

### Send SMS Notification

```json
{
  "type": "SMS",
  "recipient": "+1234567890",
  "message": "Your verification code is 123456"
}
```

### Health Check

**Endpoint:** `GET /api/v1/notifications/health`

**Response:** `200 OK` - "Notification Service is running"

## Testing with cURL

### Send Email Notification

```bash
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "type": "EMAIL",
    "recipient": "test@example.com",
    "subject": "Test Email",
    "message": "This is a test notification"
  }'
```

### Send SMS Notification

```bash
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "type": "SMS",
    "recipient": "+1234567890",
    "message": "Test SMS notification"
  }'
```

## Configuration

### Email Configuration

Update `application.properties` with your SMTP credentials:

```properties
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

For Gmail, you need to generate an App Password:
1. Enable 2-factor authentication
2. Go to Google Account settings
3. Generate an App Password for "Mail"

### Kafka Configuration

The default configuration connects to Kafka at `localhost:9092`. Update in `application.properties`:

```properties
spring.kafka.bootstrap-servers=your-kafka-server:9092
kafka.topic.notification=notification-events
```

## Architecture Highlights

### Event-Driven Design

The system uses **Apache Kafka** as a message broker to decouple notification creation from delivery:

1. **Producer Side**: REST API receives notification request and publishes event to Kafka
2. **Consumer Side**: Kafka consumer processes events asynchronously and delivers notifications

### Benefits

- **Scalability**: Scale producers and consumers independently
- **Reliability**: Messages persisted in Kafka until processed
- **Performance**: Non-blocking async processing
- **Decoupling**: Services don't need to know about each other
- **Fault Tolerance**: Automatic retries and dead letter queue

### Kafka Topics

- **notification-events**: Main topic for all notification events
- **Partitions**: 3 partitions for parallel processing
- **Replication Factor**: 1 (increase in production)

## Monitoring

### Kafka UI (Optional)

Access the Kafka UI at `http://localhost:8090` to monitor:
- Topics and partitions
- Consumer groups
- Message flow
- Lag monitoring

### Application Logs

The application logs provide detailed information about:
- Kafka message production and consumption
- Notification delivery status
- Error handling and retries

## Future Enhancements

- [ ] Implement Dead Letter Queue (DLQ) for failed notifications
- [ ] Add notification status tracking and querying
- [ ] Integrate with real SMS providers (Twilio, AWS SNS)
- [ ] Add push notification support (FCM, APNs)
- [ ] Implement notification templates
- [ ] Add metrics and monitoring (Prometheus, Grafana)
- [ ] Database persistence for notification history
- [ ] Add authentication and authorization
- [ ] Implement rate limiting
- [ ] Add webhook support for delivery status callbacks

## Contributing

This is a personal portfolio project demonstrating event-driven architecture and Kafka integration. Feel free to fork and adapt for your own use cases!

## License

MIT License - feel free to use this project for learning and portfolio purposes.

---

**Built by:** [Your Name]  
**Contact:** [Your Email]  
**LinkedIn:** [Your LinkedIn Profile]  
**GitHub:** [Your GitHub Profile]
