# 🏥 CareBridge Backend

CareBridge Backend is a REST API for healthcare facility management. It features a **Hybrid Architecture** that combines traditional hand-coded controllers with a **Universal CRUD Framework** for rapid development.

Built with **Java 21** and **Spring Boot 3.4.1**, it leverages Spring Data JPA, Spring Security, and Liquibase for database migrations.

---

## 🚀 Key Features

- **Hybrid API Architecture**: Use traditional DAOs/Controllers for complex business logic, and the Universal CRUD engine for standard data operations.
- **Universal CRUD (v3)**: Automatically generates RESTful endpoints for any `@CrudResource`-annotated entity using reflection.
- **JWT-Based Security**: Robust authentication and Role-Based Access Control (RBAC) via Spring Security.
- **Database Migrations**: Schema managed with Liquibase.
- **Modern Tech Stack**: Java 21, Spring Boot 3.4.1, Spring Data JPA, Spring Security.
- **Comprehensive Testing**: Integration tests using JUnit 5, Rest-Assured, and H2 in-memory database.

---

## 🛠 Tech Stack

- **Runtime**: [Java 21](https://adoptium.net/temurin/releases/?version=21)
- **Framework**: [Spring Boot 3.4.1](https://spring.io/projects/spring-boot)
- **ORM**: Spring Data JPA (Hibernate)
- **Database**: [PostgreSQL 42.7.10](https://jdbc.postgresql.org/)
- **Migrations**: Liquibase
- **Security**: Spring Security, JWT (nimbus-jose-jwt 10.5) & BCrypt (jbcrypt)
- **Utilities**: Lombok, Reflections 0.10.2
- **Testing**: JUnit 5, Rest-Assured, H2

---

## 📦 Project Structure

```text
carebridge-backend/
├── src/main/java/com/carebridge/
│   ├── config/                 # Spring beans, database seeding
│   ├── controllers/
│   │   ├── impl/               # Hand-coded REST controllers
│   │   └── security/           # Authentication controller
│   ├── crud/
│   │   ├── annotations/        # @CrudResource, @DynamicDTO, @ExcludeFromDTO, etc.
│   │   ├── api/                # UniversalCrudController
│   │   ├── data/               # BaseEntity, GenericRepository
│   │   └── logic/              # DynamicCrudManager, MappingService, DynamicDtoAdvice
│   ├── dao/                    # Data Access Objects
│   ├── entities/               # JPA models
│   ├── enums/                  # Role, EntryType, RiskAssessment
│   ├── exceptions/             # Global exception handling
│   ├── security/               # JwtFilter, TokenSecurity, SecurityConfig
│   ├── utils/                  # Utility helpers
│   └── CareBridgeApplication.java  # Main entry point
├── src/main/resources/
│   ├── db/changelog/           # Liquibase migration scripts
│   └── application-example.properties
├── docker-compose.yml          # Local PostgreSQL via Docker
└── pom.xml                     # Maven dependencies and build config
```

---

## ✨ Universal CRUD (v3)

Any entity annotated with `@CrudResource` is automatically registered on startup. The system:
1. Auto-discovers the entity via reflection.
2. Registers a generic `BaseService` backed by Spring's `EntityManager`.
3. Generates a full suite of REST endpoints under `/api/v3/{resource}`.

**Example Endpoint:**
`GET /api/v3/residents` → Returns all residents.

---

## 🚦 Quick Start

### 1. Prerequisites
- **JDK 21+**
- **Maven 3.9+**
- **PostgreSQL** (local, [Docker](#docker), or [Neon.tech](https://neon.tech))

### 2. Configure Application Properties
Copy the example config and fill in your values:
```bash
cp src/main/resources/application-example.properties src/main/resources/application.properties
```

`application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/carebridge
spring.datasource.username=postgres
spring.datasource.password=yourpassword

server.servlet.context-path=/api

carebridge.jwt.issuer=carebridge
carebridge.jwt.secret=REPLACE_WITH_A_LONG_RANDOM_SECRET_KEY_AT_LEAST_32_CHARS
carebridge.jwt.expire=3600000
```

### 3. Build & Run
```bash
mvn clean install
mvn spring-boot:run
```

The API will be available at `http://localhost:8080/api`.

### Docker

A `docker-compose.yml` is included to start a local PostgreSQL instance:
```bash
docker compose up -d
```
This starts PostgreSQL on port `5432` with database `cruddb`, user `user`, and password `password`. Update `application.properties` accordingly.

---

## 🧪 Testing

Tests run against an H2 in-memory database and do not require Docker or a running PostgreSQL instance.

```bash
mvn test
```

---

## 🛡 Security

Access control is handled via `SecurityConfig`, `JwtFilter`, and `TokenSecurity`.
- **Authentication**: `POST /api/auth/login`
- **Authorization**: Role-based checks via Spring Security (ADMIN, USER, etc.).

---

## 🛣 API Endpoints

All endpoints are prefixed with `/api` (context path).

### 🔐 Authentication (`/auth`)
| Method | Path | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/auth/login` | Log in and receive a JWT token | Public |
| `POST` | `/auth/register` | Register a new user | Public |
| `GET` | `/auth/healthcheck` | Verify the API status | Public |

### 🚀 Universal CRUD v3 (`/v3`)
*`{resource}` can be any registered entity path (e.g., `users`, `residents`, `journals`, `journal-entries`).*

| Method | Path | Description |
| :--- | :--- | :--- |
| `GET` | `/v3/metadata` | Get field and type info for all entities |
| `GET` | `/v3/{resource}` | Get all items (supports `page` & `size`) |
| `GET` | `/v3/{resource}/{id}` | Get a single item by ID |
| `POST` | `/v3/{resource}` | Create a new item |
| `PUT` | `/v3/{resource}/{id}` | Update an existing item |
| `DELETE` | `/v3/{resource}/{id}` | Remove an item |

### 👥 User Management (`/users`)
| Method | Path | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/users` | List all users | Authenticated |
| `GET` | `/users/{id}` | Get a specific user | Authenticated |
| `GET` | `/users/me` | Get the currently authenticated user | Authenticated |
| `POST` | `/users` | Create a new user | Authenticated |
| `PUT` | `/users/{id}` | Update a user | Authenticated |
| `DELETE` | `/users/{id}` | Delete a user | Authenticated |
| `POST` | `/users/populate` | Seed the database with initial data | Public |
| `POST` | `/users/{id}/link-residents` | Assign residents to a guardian | Authenticated |

### 🏥 Residents (`/residents`)
| Method | Path | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/residents` | List all residents | Authenticated |
| `GET` | `/residents/{id}` | Get a resident by ID | Authenticated |
| `GET` | `/residents/cpr/{cpr}` | Get a resident by CPR number | Authenticated |
| `POST` | `/residents/create` | Create a resident (auto-creates linked journal) | Authenticated |

### 📓 Journal Entries (`/journals/{journalId}/journal-entries`)
| Method | Path | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/journals/{jid}/journal-entries` | List entry IDs for a journal | Authenticated |
| `GET` | `/journals/{jid}/journal-entries/{id}` | Get a specific journal entry | Authenticated |
| `POST` | `/journals/{jid}/journal-entries` | Create a new journal entry | Authenticated |
| `PUT` | `/journals/{jid}/journal-entries/{id}` | Update a journal entry | Authenticated |

---

## 📖 Detailed Setup
For a step-by-step guide on setting up this project, see [setup.md](./setup.md).
