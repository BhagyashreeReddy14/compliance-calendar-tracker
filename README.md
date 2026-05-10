# Compliance Calendar Tracker

A production-ready **Spring Boot 3 (Java 17)** backend application for managing and tracking compliance records efficiently. Built following clean architecture, industry-level coding standards, and security best practices.

---

## 🚀 Features

- **Robust Security**: Stateless JWT authentication with BCrypt password hashing.
- **Role-Based Access Control**: `ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_VIEWER`.
- **Compliance Management**: Full CRUD with soft-delete pattern, pagination, sorting, and JPQL-powered search.
- **Audit Logging**: Tamper-evident tracking of all CREATE, UPDATE, and DELETE actions.
- **Performance Optimized**: Redis caching layer for GET requests with intelligent eviction policies.
- **Email Notifications**: Async email alerts via Thymeleaf templates for record creation and daily overdue alerts.
- **File Handling**: Secure multipart file uploads with path traversal guards and MIME-type validation.
- **API Documentation**: Fully documented using Swagger/OpenAPI 3.
- **Production Ready**: Containerized via Docker with multi-stage builds and a `docker-compose` setup.
- **High Test Coverage**: Extensive JUnit 5 + Mockito test suites with JaCoCo coverage reporting (>80%).

---

## 🏗️ Architecture

```text
       +-------------------+       +-------------------+       +-------------------+
       |    Frontend UI    |       |     AI Service    |       |     External      |
       |  (React + Vite)   |<----->|  (Flask + Python) |<----->|   (Groq LLM API)  |
       +-------------------+       +-------------------+       +-------------------+
                 ^                           ^                           |
                 | HTTP / Axios              | HTTP / JSON               v
                 v                           v                  +-------------------+
       +---------------------------------------------------+    |     ChromaDB      |
       |               Spring Boot 3 Backend               |--->|  (Vector Store)   |
       |   +-------------+  +-----------+  +-------------+ |    +-------------------+
       |   | Controllers |  | Services  |  | Repositories| |
       |   +-------------+  +-----------+  +-------------+ |
       +---------------------------------------------------+
                 ^                           ^
                 | JPA / JDBC                | Redis Protocol
                 v                           v
       +-------------------+       +-------------------+
       |    PostgreSQL     |       |       Redis       |
       |  (Primary DB)     |       | (Cache & Session) |
       +-------------------+       +-------------------+
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.2.5 |
| **Database** | PostgreSQL 15 (Prod) / H2 (Test) |
| **Migrations** | Flyway |
| **Security** | Spring Security + jjwt (0.12.6) |
| **Caching** | Redis 7 |
| **Email** | JavaMailSender + Thymeleaf |
| **Documentation** | springdoc-openapi (Swagger 3) |
| **Testing** | JUnit 5, Mockito, JaCoCo |
| **Build Tool** | Maven 3.9.x |

---

## 🐳 Running with Docker (Recommended)

Since the project requires PostgreSQL and Redis, the easiest way to run it is using Docker Compose. This completely skips the need for a local Maven installation.

### 1. Prerequisites
- Docker & Docker Compose installed on your system.

### 2. Start the Stack
Run the following command in the project root directory:

```bash
docker-compose up --build -d
```

This will start:
1. PostgreSQL database (`compliance_db`) on port `5432`
2. Redis cache on port `6379`
3. Spring Boot Backend API on port `8080`

### 3. Check Logs
```bash
docker-compose logs -f app
```

### Expected Startup Output
```
Started ComplianceTrackerApplication in X.XXX seconds
Inserted 3 demo users (admin, manager, viewer) successfully.
Inserted 30 compliance records successfully.
```

---

## 💻 Running Locally (Development)

If you have **Java 17** and **Maven** installed locally, you can run the app in development mode (which uses an in-memory H2 database by default):

```bash
mvn spring-boot:run
```

*(Note: If you encounter `mvn is not recognized`, please ensure Maven is added to your system's PATH variable, or use the Docker method above).*

---

## ⚙️ Environment Variables (.env)

The project requires several environment variables for production execution. Create a `.env` file in the root directory using `.env.example` as a template.

| Variable | Description | Example Value |
|---|---|---|
| `DB_URL` | JDBC URL for PostgreSQL | `jdbc:postgresql://db:5432/compliance_db` |
| `DB_USER` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `postgres` |
| `REDIS_HOST` | Redis server hostname | `redis` |
| `REDIS_PORT` | Redis server port | `6379` |
| `JWT_SECRET` | 256-bit secret key for JWT signing | `mySecretKey12345678901234567890...` |
| `JWT_EXPIRATION` | JWT token expiration time in ms | `86400000` |
| `MAIL_HOST` | SMTP server host | `smtp.gmail.com` |
| `MAIL_PORT` | SMTP server port | `587` |
| `MAIL_USERNAME` | SMTP authentication username | `your-email@gmail.com` |
| `MAIL_PASSWORD` | SMTP authentication password | `your-app-password` |
| `NOTIFICATION_EMAIL`| Recipient for admin notifications | `admin@example.com` |

---

## 🔑 Demo Credentials (Auto-Seeded)

The application automatically seeds 3 users and 30 sample compliance records on first startup.

| Username | Email | Password | Role |
|---|---|---|---|
| `admin` | admin@example.com | `admin123` | ROLE_ADMIN |
| `manager` | manager@example.com | `manager123` | ROLE_MANAGER |
| `viewer` | viewer@example.com | `viewer123` | ROLE_VIEWER |

---

## 📚 API Documentation

Once the application is running, the interactive Swagger UI is available at:

👉 **http://localhost:8080/swagger-ui.html**

1. Go to the Swagger UI.
2. Under `Authentication`, use the `/auth/login` endpoint to get a JWT token.
3. Click the **Authorize** button at the top of the screen.
4. Enter `Bearer <your_token>` and click Authorize.
5. You can now test all secured endpoints!

---

## 📡 API Endpoints Overview

### Authentication (Public)
- `POST /auth/register` — Register a new user (`ROLE_VIEWER`)
- `POST /auth/login` — Authenticate and receive a JWT

### Compliance Records (Secured)
- `GET /api/compliance` — Get paginated/sorted records (All Roles)
- `GET /api/compliance/{id}` — Get single record (All Roles)
- `POST /api/compliance` — Create record (Admin/Manager)
- `PUT /api/compliance/{id}` — Update record (Admin/Manager)
- `DELETE /api/compliance/{id}` — Soft delete record (Admin Only)
- `GET /api/compliance/search?q=` — Case-insensitive search (All Roles)
- `GET /api/compliance/stats` — Metrics dashboard (All Roles)

### Files (Secured)
- `POST /api/files/upload` — Upload file (Admin/Manager)
- `GET /api/files/{id}` — Download file (All Roles)

---

## 🧪 Testing

To run the full test suite and generate a JaCoCo coverage report:

```bash
mvn clean verify
```
*The JaCoCo coverage report will be generated at `target/site/jacoco/index.html`.*

---

## 📁 Project Structure

```text
src/main/java/com/example/tool/
├── config/        # Security, JWT, Redis, OpenAPI configs
├── controller/    # REST API endpoints
├── dto/           # Data Transfer Objects & Validation
├── entity/        # JPA Entities (Compliance, User, AuditLog)
├── exception/     # Global Exception Handler
├── repository/    # Spring Data JPA Repositories
├── scheduler/     # Cron jobs (Overdue marking)
├── seeder/        # Database initialization (Demo data)
├── service/       # Business Logic, Caching, Audit, Emails
└── util/          # Shared stateless utilities (DateUtil)
```

---

## 🛡️ Best Practices Implemented
- **Clean Architecture**: Strict separation of concerns (Controller → Service → Repository).
- **Security**: Passwords hashed with BCrypt. Stateless JWT tokens. No session fixation.
- **Caching**: `@Cacheable` and `@CacheEvict` annotations heavily optimize read-heavy operations.
- **Soft Deletes**: Data is never hard-deleted; a boolean flag is toggled for compliance retention.
- **Transactions**: Write operations are explicitly marked with `@Transactional` to ensure data integrity.
- **Audit Trails**: Every mutation logs a before/after snapshot via Jackson serialization.
