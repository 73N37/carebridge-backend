# 🛠️ CareBridge Backend: Setup & Documentation Guide

Welcome to the CareBridge Backend. This project is a modern **Spring Boot 3** application featuring a "Zero-Code" dynamic CRUD architecture that eliminates the need for manual DTO creation.

---

## 🏗️ Section 1: Prerequisites

To run and develop this project, you need to install the following tools:

### 1. Java 21+ Development Kit (JDK)
- **What**: The core runtime and compiler for Java.
- **Download**: [Eclipse Temurin (OpenJDK 21)](https://adoptium.net/temurin/releases/?version=21)
- **Verify**: Run `java -version` in your terminal. You should see `openjdk version "21.x.x"`.

### 2. Apache Maven 3.9+
- **What**: The build automation tool used to manage dependencies and package the application.
- **Download**: [Maven Downloads](https://maven.apache.org/download.cgi)
- **Verify**: Run `mvn -v` in your terminal.

### 3. Docker Desktop (Optional but Recommended)
- **What**: Used for running the production-like PostgreSQL database locally.
- **Download**: [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- **Note**: The project includes a "Testcontainers" integration that spins up a real database during tests automatically if Docker is running.

---

## ⚙️ Section 2: How the Code Works

This application uses a unique **"Virtual Record"** architecture. Instead of writing DTO (Data Transfer Object) classes for every entity, the system generates them dynamically.

### 1. Zero-Code CRUD Logic
- **Discovery**: On startup, the `DynamicCrudManager` scans the `com.carebridge.entities` package for classes annotated with `@CrudResource`.
- **Registration**: For every discovered entity, it builds a metadata model and initializes a generic `BaseService`.
- **Dynamic Routing**: The `UniversalCrudController` uses a "Catch-All" path (`/api/v3/{resource}`) to route requests to the correct dynamic service based on the URL path.

### 2. Transparent Response Transformation
We use the **@DynamicDTO** pattern to keep controllers clean:
- Controllers return raw **Entities** or **Lists**.
- The `DynamicDtoAdvice` intercepts every response marked with `@DynamicDTO`.
- It uses the `MappingService` to transform the Entity into a `Map<String, Object>`.
- During this process, it checks for `@ExcludeFromDTO` annotations on entity fields to hide sensitive data (like password hashes).

### 3. Security Flow
- **JWT Filter**: Every request passes through `JwtFilter`. It validates the `Authorization: Bearer <token>` header.
- **Spring Security**: The authenticated user is placed into the `SecurityContext`, allowing role-based access control (RBAC).

---

## 📖 Section 3: Detailed Documentation of Code Components

### 📦 `com.carebridge`
- **`CareBridgeApplication.java`**: The main entry point. Bootstraps Spring Boot, initializes the component scan, and enables JPA repositories.

### 📦 `com.carebridge.config`
- **`ApplicationConfig.java`**: Configures global beans, specifically the Jackson `ObjectMapper` with support for Java Time types.
- **`Populator.java`**: A utility service that seeds the database with initial event types and an administrator account.
- **`PopulatorRunner.java`**: A `CommandLineRunner` that triggers the `Populator` when the app starts (disabled in the `test` profile).

### 📦 `com.carebridge.crud.annotations`
- **`@CrudResource`**: Marks an entity to be handled by the Universal CRUD system. Defines its API path (e.g., `/residents`).
- **`@DynamicDTO`**: Placed on controller methods to trigger the automatic Entity-to-Map conversion.
- **`@ExcludeFromDTO`**: Security annotation. Fields with this (like `User.passwordHash`) will never appear in API responses.
- **`@ExcludeFromMeta`**: Hides internal technical fields from the `/api/v3/metadata` endpoint.

### 📦 `com.carebridge.crud.api`
- **`UniversalCrudController.java`**: The heart of the zero-code API. Dynamically handles `GET`, `POST`, `PUT`, and `DELETE` for any registered entity.

### 📦 `com.carebridge.crud.logic`
- **`DynamicCrudManager.java`**: Manages the lifecycle of dynamic resources. Uses reflection to build metadata for the UI.
- **`MappingService.java`**: The conversion engine. Safely flattens complex Hibernate entities into JSON-friendly maps, resolving circular references and lazy-loading issues.
- **`DynamicDtoAdvice.java`**: A Spring `ResponseBodyAdvice` that acts as a global interceptor for response formatting.

### 📦 `com.carebridge.crud.logic.core`
- **`BaseService.java`**: A truly generic service that interacts directly with the `EntityManager` to perform database operations without needing custom repositories.

### 📦 `com.carebridge.dao`
- **`IDAO.java`**: The base interface for all Data Access Objects.
- **`UserDAO`, `EventDAO`, etc.**: Standard Spring `@Repository` components for hand-coded logic that goes beyond simple CRUD.

### 📦 `com.carebridge.entities`
- **`BaseEntity.java`**: The root class for all models. Provides the `id` field and standard identity strategy.
- **`User`, `Resident`, `Event`, etc.**: JPA entities representing the business domain.

### 📦 `com.carebridge.security`
- **`SecurityConfig.java`**: Defines the security filter chain, CORS policy, and public/private endpoint rules.
- **`JwtFilter.java`**: Intercepts requests to verify JWT tokens and populate the authenticated user context.
- **`TokenSecurity.java`**: Core logic for generating and parsing signed JWT tokens using the Nimbus library.
