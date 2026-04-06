# Event-Driven Notification System - Setup & Run Guide

## Prerequisites

Before running this project, ensure you have the following installed:

### 1. Java Development Kit (JDK 21)

**Windows:**
- Download JDK 21 from [Adoptium (Eclipse Temurin)](https://adoptium.net/temurin/releases/?version=21) or [Oracle JDK](https://www.oracle.com/java/technologies/downloads/#java21)
- Run the installer
- Set environment variables:
  ```powershell
  # Set JAVA_HOME (replace path with your installation directory)
  setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-21.0.x.x-hotspot"
  
  # Add to PATH
  setx PATH "%PATH%;%JAVA_HOME%\bin"
  ```
- Restart your terminal/PowerShell
- Verify installation:
  ```bash
  java -version
  ```
  Expected output: `openjdk version "21.x.x"` or similar

**macOS/Linux:**
```bash
# Using SDKMAN (recommended)
curl -s "https://get.sdkman.io" | bash
sdk install java 21-tem

# Verify
java -version
```

### 2. Docker Desktop

**Windows:**
- Download from [Docker Desktop for Windows](https://www.docker.com/products/docker-desktop/)
- Install and start Docker Desktop
- Ensure Docker Engine is running (check system tray icon)
- Verify installation:
  ```bash
  docker --version
  docker-compose --version
  ```

**macOS:**
- Download from [Docker Desktop for Mac](https://www.docker.com/products/docker-desktop/)
- Install and start Docker Desktop

**Linux:**
```bash
# Install Docker Engine
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Install Docker Compose
sudo apt-get install docker-compose-plugin
```

### 3. Maven (Optional - project includes Maven Wrapper)

The project includes Maven Wrapper (`mvnw`), so you don't need to install Maven separately.

---

## Step-by-Step Setup Instructions

### Step 1: Clone/Navigate to Project Directory

```bash
cd C:\Users\ChakriDurgaAravindDa\Projects\notification
```

### Step 2: Start Docker Desktop

1. Open Docker Desktop application
2. Wait until Docker Engine is running (green status indicator)
3. Verify Docker is running:
   ```bash
   docker ps
   ```

### Step 3: Start Kafka Infrastructure

```bash
# Start Zookeeper, Kafka, and Kafka UI
docker-compose up -d
```

**Verify services are running:**
```bash
docker-compose ps
```

Expected output:
```
NAME         IMAGE                              STATUS
kafka        confluentinc/cp-kafka:latest      Up (healthy)
kafka-ui     provectuslabs/kafka-ui:latest     Up (healthy)
zookeeper    confluentinc/cp-zookeeper:latest  Up (healthy)
```

**Wait for services to be healthy** (may take 30-60 seconds):
```bash
# Check logs if needed
docker-compose logs -f
```

Press `Ctrl+C` to exit logs.

### Step 4: Configure Email Settings (Optional)

If you want to test actual email sending:

1. Open `src/main/resources/application.properties`
2. Update email configuration:
   ```properties
   spring.mail.username=your-email@gmail.com
   spring.mail.password=your-app-password
   ```

**For Gmail:**
- Enable 2-factor authentication
- Generate an App Password: [Google Account Settings](https://myaccount.google.com/apppasswords)
- Use the generated password (not your regular password)

**Note:** Email will work in mock mode without configuration for development/testing.

### Step 5: Build the Project

```bash
# Windows
.\mvnw.cmd clean install

# macOS/Linux
./mvnw clean install
```

**Expected output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: X s
```

### Step 6: Run the Application

```bash
# Windows
.\mvnw.cmd spring-boot:run

# macOS/Linux  
./mvnw spring-boot:run

# OR with development profile
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

**Expected output:**
```
Started NotificationApplication in X seconds
```

### Step 7: Verify Application is Running

Open browser or use curl:

**Health Check:**
```bash
curl http://localhost:8080/api/v1/notifications/health
```
Expected: `Notification Service is running`

**Actuator Health:**
```bash
curl http://localhost:8080/actuator/health
```
Expected: JSON response with status "UP"

---

## Accessing Services

Once the application is running, you can access:

| Service | URL | Description |
|---------|-----|-------------|
| **Notification API** | http://localhost:8080/api/v1/notifications | REST API endpoint |
| **Health Check** | http://localhost:8080/api/v1/notifications/health | Simple health check |
| **Actuator Health** | http://localhost:8080/actuator/health | Detailed health info |
| **Actuator Metrics** | http://localhost:8080/actuator/metrics | Application metrics |
| **Prometheus Metrics** | http://localhost:8080/actuator/prometheus | Prometheus format |
| **H2 Database Console** | http://localhost:8080/h2-console | Database viewer |
| **Kafka UI** | http://localhost:8090 | Monitor Kafka topics |

### H2 Database Console Access

- **URL**: http://localhost:8080/h2-console
- **JDBC URL**: `jdbc:h2:mem:notificationdb`
- **Username**: `sa`
- **Password**: (leave empty)

---

## Testing the Application

### 1. Send Email Notification

```bash
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "type": "EMAIL",
    "recipient": "test@example.com",
    "subject": "Test Email",
    "message": "This is a test notification from the system"
  }'
```

**Expected Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PENDING",
  "message": "Notification queued for processing",
  "timestamp": "2026-04-06T10:30:00"
}
```

### 2. Send SMS Notification

```bash
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "type": "SMS",
    "recipient": "+1234567890",
    "message": "Your verification code is 123456"
  }'
```

### 3. Test with Invalid Data (Validation)

```bash
# Invalid email format
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "type": "EMAIL",
    "recipient": "invalid-email",
    "message": "Test"
  }'
```

**Expected Response:** `400 Bad Request` with validation error details

### 4. Monitor Kafka Messages

1. Open Kafka UI: http://localhost:8090
2. Navigate to Topics → `notification-events`
3. View messages being produced and consumed
4. Check `notification-dlq` topic for failed notifications

### 5. View Database Records

1. Open H2 Console: http://localhost:8080/h2-console
2. Connect with credentials above
3. Query notifications:
   ```sql
   SELECT * FROM NOTIFICATIONS ORDER BY CREATED_AT DESC;
   ```

---

## Running Tests

### Run All Tests

```bash
# Windows
.\mvnw.cmd test

# macOS/Linux
./mvnw test
```

### Run Specific Test Class

```bash
.\mvnw.cmd test -Dtest=NotificationControllerTest
```

### Run with Coverage Report

```bash
.\mvnw.cmd clean test jacoco:report
```

---

## Troubleshooting

### Issue: "JAVA_HOME not set"

**Solution:**
```powershell
# Check current JAVA_HOME
echo $env:JAVA_HOME

# Set it if missing (replace with your path)
setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-21.0.x.x-hotspot"

# Restart terminal
```

### Issue: Docker services not starting

**Solution:**
```bash
# Check Docker Desktop is running
docker ps

# If services fail, check logs
docker-compose logs

# Restart services
docker-compose down
docker-compose up -d
```

### Issue: Port already in use

**Solution:**
```bash
# Check what's using port 8080
netstat -ano | findstr :8080

# Kill the process (replace PID)
taskkill /PID <PID> /F

# Or change application port in application.properties
server.port=8081
```

### Issue: Kafka connection refused

**Solution:**
1. Ensure Docker services are healthy: `docker-compose ps`
2. Wait 30-60 seconds for Kafka to fully start
3. Check Kafka logs: `docker-compose logs kafka`
4. Restart if needed: `docker-compose restart kafka`

### Issue: Email sending fails

**Solution:**
- For development, email failures are logged but don't break the system
- Check logs for details: application will continue with mock email provider
- Configure real SMTP settings if needed (see Step 4)

### Issue: Database connection error

**Solution:**
- H2 runs in-memory, should work automatically
- Check logs for specific error
- Verify JPA dependency is present in pom.xml
- Try: `.\mvnw.cmd clean install` to rebuild

---

## Stopping the Application

### Stop Spring Boot Application

Press `Ctrl+C` in the terminal where the application is running

### Stop Docker Services

```bash
# Stop services (keeps data in volumes)
docker-compose stop

# Stop and remove containers (data persists in volumes)
docker-compose down

# Stop and remove everything including volumes
docker-compose down -v
```

---

## Development Mode

For development with auto-reload and debug logging:

```bash
# Run with dev profile
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev

# Enable debug mode
.\mvnw.cmd spring-boot:run -Ddebug
```

**Development profile features:**
- DEBUG level logging
- H2 console enabled
- Show SQL queries
- More aggressive retry settings for testing

---

## Production Deployment Checklist

Before deploying to production:

- [ ] Update email credentials in environment variables
- [ ] Change database from H2 to PostgreSQL
- [ ] Update Kafka bootstrap servers
- [ ] Increase Kafka replication factor (min 3)
- [ ] Configure real SMS provider (Twilio, AWS SNS)
- [ ] Set up monitoring (Prometheus + Grafana)
- [ ] Configure SSL/TLS for Kafka
- [ ] Add authentication/authorization
- [ ] Set up log aggregation
- [ ] Configure backup strategy
- [ ] Set resource limits (memory, CPU)
- [ ] Enable rate limiting
- [ ] Review security settings

---

## Quick Start (TL;DR)

```bash
# 1. Ensure Java 21 and Docker Desktop are installed and running

# 2. Start infrastructure
docker-compose up -d

# 3. Wait for services to be healthy (30-60 seconds)
docker-compose ps

# 4. Build and run
.\mvnw.cmd clean install
.\mvnw.cmd spring-boot:run

# 5. Test
curl http://localhost:8080/api/v1/notifications/health

# 6. Send test notification
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{"type":"EMAIL","recipient":"test@example.com","subject":"Test","message":"Hello"}'
```

---

## Additional Resources

- **Kafka UI**: http://localhost:8090 - Monitor topics and messages
- **H2 Console**: http://localhost:8080/h2-console - View database
- **Actuator Docs**: https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html
- **Spring Kafka Docs**: https://docs.spring.io/spring-kafka/reference/html/

---

## Support

If you encounter issues:

1. Check the logs: `docker-compose logs` and application console output
2. Verify all prerequisites are installed and running
3. Ensure ports 8080, 8090, 9092, 2181 are available
4. Review the Troubleshooting section above

For questions or issues, check the project README.md for contact information.
