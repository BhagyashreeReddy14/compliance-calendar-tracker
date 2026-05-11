# Compliance Calendar and Tracker - Backend

This is the backend implementation for the Compliance Calendar and Tracker project.

## Tech Stack
- **Java 17**
- **Spring Boot 3.2.5**
- **PostgreSQL** (Database)
- **Redis** (Caching)
- **Spring Security + JWT** (Authentication)
- **Flyway** (DB Migration)
- **Swagger/OpenAPI** (Documentation)
- **JavaMailSender** (Notifications)
- **Maven** (Build Tool)
- **Docker & Docker Compose**

## Features
- Full CRUD for Compliance Records
- Pagination, Sorting, and Search
- Role-based Access Control (ADMIN, MANAGER, VIEWER)
- JWT Authentication (Access & Refresh Tokens)
- Redis Caching for performance
- Email Notifications for record assignment and overdue alerts
- File Upload support
- AI Integration: Record description, Recommendations, and PDF Report Generation
- Dashboard Statistics
- Automated DB migrations with Flyway
- Swagger Documentation

## Setup & Installation

### Prerequisites
- JDK 17
- Docker & Docker Compose
- Maven (or use bundled)

### Running with Docker Compose
```bash
docker-compose up --build
```

### Running Locally
1. Configure environment variables in `application.yml` or set them in your environment.
2. Start PostgreSQL and Redis.
3. Run the application:
```bash
mvn spring-boot:run
```

## API Documentation
Once the application is running, access Swagger UI at:
`http://localhost:8080/swagger-ui.html`

## Default Credentials (Seeded)
- **Admin**: `admin@example.com` / `admin123`
- **Manager**: `manager@example.com` / `manager123`

## Environment Variables
- `DB_HOST`: Database host (default: localhost)
- `DB_PORT`: Database port (default: 5432)
- `DB_NAME`: Database name (default: compliance_db)
- `DB_USERNAME`: Database username (default: postgres)
- `DB_PASSWORD`: Database password (default: postgres)
- `REDIS_HOST`: Redis host (default: localhost)
- `REDIS_PORT`: Redis port (default: 6379)
- `JWT_SECRET`: Secret key for JWT signing
- `MAIL_USERNAME`: Email for sending notifications
- `MAIL_PASSWORD`: Password for email account
