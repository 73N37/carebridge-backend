# 🛠 Setup Guide: CareBridge Backend

This guide explains how to set up and deploy the CareBridge Backend on your own server.

## 1. Prerequisites

Ensure your server has the following installed:
- **Java 25 (OpenJDK)**: The project requires Java 25.
- **Maven 3.9+**: For building the project.
- **PostgreSQL 15+**: Or a managed service like [Neon.tech](https://neon.tech).
- **Git**: To clone the repository.

---

## 2. Database Setup

### Option A: Local PostgreSQL
1.  Install PostgreSQL on your server.
2.  Create a new database and user:
    ```sql
    CREATE DATABASE carebridge;
    CREATE USER careadmin WITH PASSWORD 'secure_password';
    GRANT ALL PRIVILEGES ON DATABASE carebridge TO careadmin;
    ```

### Option B: Managed Database (Neon.tech) - Recommended
1.  Create a project at [Neon.tech](https://neon.tech).
2.  Copy your connection details (Host, Username, Password).
3.  Ensure SSL is supported (Neon requires `sslmode=require`).

---

## 3. Environment Configuration

The application uses a `.env` file for configuration. Create a file named `.env` in the project root:

```env
# Server Configuration
SERVER_PORT=7070

# Database Configuration
DB_HOST=localhost:5432
DB_NAME=carebridge
DB_USER=careadmin
DB_PASSWORD=secure_password
DB_SSLMODE=disable # Use 'require' for Neon.tech or production
DB_DDL_AUTO=update # 'create' will wipe data on startup, 'update' preserves it
DB_SHOW_SQL=false

# Security
JWT_SECRET=generate_a_long_random_string_here
TOKEN_EXPIRE_TIME=3600000

# Feature Flags
USE_H2=false # Set to true for a memory-only DB (dev only)
```

---

## 4. Building the Project

Run the following command to download dependencies and build the executable JAR:

```bash
mvn clean package -DskipTests
```

This will create a JAR file in the `target/` directory:
`target/carebridge-backend-1.0-SNAPSHOT.jar`

---

## 5. Running the Application

### Manual Start
```bash
java -jar target/carebridge-backend-1.0-SNAPSHOT.jar
```

### Running as a Background Service (systemd)
For production on Linux, it is recommended to use `systemd`. Create a service file at `/etc/systemd/system/carebridge.service`:

```ini
[Unit]
Description=CareBridge Backend Service
After=network.target

[Service]
User=your-user
WorkingDirectory=/path/to/carebridge-backend
ExecStart=/usr/bin/java -jar /path/to/carebridge-backend/target/carebridge-backend-1.0-SNAPSHOT.jar
Restart=always

[Install]
WantedBy=multi-user.target
```

Then start the service:
```bash
sudo systemctl daemon-reload
sudo systemctl enable carebridge
sudo systemctl start carebridge
```

---

## 6. Accessing the API

Once running, the API will be available at:
`http://your-server-ip:7070/api`

- **Health Check**: `GET /`
- **Universal CRUD**: `GET /api/v3/resident`
- **Authentication**: `POST /api/auth/login`

---

## 🧪 Running Tests

The project includes a comprehensive suite of RESTful API tests. By default, they use an **H2 In-Memory Database** for speed and environment compatibility (no Docker required).

To run all tests:
```bash
mvn clean test
```

To run a specific test class:
```bash
mvn test "-Dtest=restTest.UserControllerTest"
```

The tests will automatically:
1. Start a Javalin server on port 7070.
2. Initialize an H2 database.
3. Populate it with initial data.
4. Run 50+ REST API scenarios.
5. Generate a JaCoCo coverage report in `target/site/jacoco/index.html`.
