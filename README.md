# HEMIS Backend v2

> Higher Education Management Information System - Modern Spring Boot Backend

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://openjdk.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18.0-blue.svg)](https://www.postgresql.org/)
[![Liquibase](https://img.shields.io/badge/Liquibase-4.31.1-red.svg)](https://www.liquibase.org/)
[![License](https://img.shields.io/badge/License-Proprietary-yellow.svg)](LICENSE)

## 📑 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Quick Start](#quick-start)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Running the Application](#running-the-application)
- [Database Migrations](#database-migrations)
- [Development](#development)
  - [Project Structure](#project-structure)
  - [Building](#building)
  - [Testing](#testing)
  - [Code Quality](#code-quality)
- [API Documentation](#api-documentation)
- [Configuration](#configuration)
- [Deployment](#deployment)
  - [Docker Deployment](#docker-deployment)
  - [Production Setup](#production-setup)
- [Monitoring](#monitoring)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

---

## 🎯 Overview

HEMIS v2.0 is a comprehensive university management system serving **200+ universities** across Uzbekistan. Built with Spring Boot 3.5.7 and modern microservices architecture, it manages student records, academic programs, diplomas, contracts, scholarships, and integrations with government services.

**Key Stats:**
- 📊 289 database tables
- 🔌 100+ REST API endpoints
- 🏢 200+ active universities
- 👥 Multi-tenant architecture
- 🔐 JWT OAuth2 authentication

---

## ✨ Features

### Core Functionality

- **👨‍🎓 Student Management** - Enrollment, academic records, transcripts
- **👨‍🏫 Academic Staff** - Teacher profiles, assignments, workload tracking
- **🏛️ Department Management** - Organizational structure, hierarchy
- **🎓 Diploma System** - Issuance, verification, blank inventory
- **💰 Financial Operations** - Contracts, scholarships, employment tracking
- **🔗 Government Integration** - Ministry APIs, data exchange
- **📊 Reporting & Analytics** - Custom reports, data export

### Technical Features

- ✅ **100% Backward Compatible** - Preserves old-hemis API contracts
- ✅ **Multi-Module Architecture** - Clean separation of concerns
- ✅ **Database Migrations** - Liquibase with native rollback support
- ✅ **API Documentation** - OpenAPI 3.0 + Swagger UI
- ✅ **Security** - JWT OAuth2 Resource Server
- ✅ **Caching** - Redis integration for performance
- ✅ **Internationalization** - 9 languages support

---

## 🏗️ Architecture

### Multi-Module Structure

```
hemis-back/
├── common/          # Shared utilities, DTOs, exceptions
├── domain/          # JPA entities, repositories, Liquibase migrations
├── security/        # JWT OAuth2 authentication & authorization
├── service/         # Business logic, caching, transactions
├── api-web/         # Public Web APIs (students, teachers, etc.)
├── api-external/    # External S2S APIs (government, education)
├── api-legacy/      # Legacy CUBA endpoints (backward compatibility)
└── app/             # Main Spring Boot application
```

### Technology Stack

| Layer | Technology | Version | Purpose |
|-------|------------|---------|---------|
| **Language** | Java | 21 LTS | Core programming language |
| **Framework** | Spring Boot | 3.5.7 | Application framework |
| **Build Tool** | Gradle | 8.10.2 | Build automation |
| **Database** | PostgreSQL | 18.0 | Primary data store |
| **ORM** | Hibernate | 6.6.33 | Object-relational mapping |
| **Migration** | Liquibase | 4.31.1 | Database version control |
| **Security** | Spring Security | 6.x | Authentication & authorization |
| **Cache** | Redis | Latest | Performance caching |
| **API Docs** | SpringDoc OpenAPI | 2.7.0 | API documentation |
| **Logging** | Logback + SLF4J | 2.x | Application logging |

### Dependency Graph

```
app
 ├─→ api-web ────────┐
 ├─→ api-external ───┼─→ service ──→ security ──→ domain ──→ common
 └─→ api-legacy ─────┘
```

---

## 🚀 Quick Start

### Prerequisites

| Tool | Version | Download |
|------|---------|----------|
| **Java JDK** | 21 LTS | [Amazon Corretto](https://aws.amazon.com/corretto/) or [OpenJDK](https://openjdk.org/) |
| **PostgreSQL** | 18.0+ | [PostgreSQL Downloads](https://www.postgresql.org/download/) |
| **Redis** | 6.0+ (optional) | [Redis Downloads](https://redis.io/download) |
| **Gradle** | 8.10.2 | Included (wrapper) |

### Installation

**1. Clone the repository:**

```bash
git clone https://github.com/hemis/hemis-backend.git
cd hemis-backend
```

**2. Create database:**

```bash
# Using createdb command
createdb test_hemis

# Or using psql
psql -U postgres -c "CREATE DATABASE test_hemis;"
```

**3. Configure environment variables:**

```bash
# Copy .env.example to .env
cp .env.example .env

# Edit .env file
export DB_MASTER_HOST=localhost
export DB_MASTER_PORT=5432
export DB_MASTER_NAME=test_hemis
export DB_MASTER_USERNAME=postgres
export DB_MASTER_PASSWORD=postgres
export JWT_SECRET=your-super-secret-key-min-64-chars-for-production
export REDIS_HOST=localhost
export REDIS_PORT=6379
```

**4. Load environment:**

```bash
source .env
```

### Running the Application

**Development mode:**

```bash
# Build and run (migrations apply automatically)
./gradlew :app:bootRun

# Or run with specific profile
./gradlew :app:bootRun --args='--spring.profiles.active=dev'
```

**Production mode:**

```bash
# Build JAR
./gradlew clean build -x test

# Run JAR
java -jar app/build/libs/app-2.1.0.jar --spring.profiles.active=prod
```

**Access the application:**

- **Application:** http://localhost:8081
- **Swagger UI:** http://localhost:8081/swagger-ui/index.html
- **OpenAPI Docs:** http://localhost:8081/v3/api-docs
- **Health Check:** http://localhost:8081/actuator/health

**Default credentials:**

```
Username: admin
Password: Admin@123
```

---

## 🗄️ Database Migrations

### Overview

HEMIS uses **Liquibase** for database version control with native rollback support (upgraded from Flyway).

**Migration files location:**
```
domain/src/main/resources/db/changelog/
├── db.changelog-master.yaml              # Master changelog
└── changesets/
    ├── 01-baseline.sql                   # V1: Create tables (DDL)
    ├── 01-baseline-rollback.sql          # V1: Rollback
    ├── 02-initial-data.sql               # V2: Seed data (DML)
    └── 02-initial-data-rollback.sql      # V2: Rollback
```

### Forward Migration

Migrations run **automatically** on application startup:

```bash
./gradlew :app:bootRun
```

Liquibase will:
1. Check `databasechangelog` table
2. Apply pending changesets
3. Update migration history
4. Log execution time

### Rollback

**Method 1: Using Liquibase CLI (recommended)**

```bash
# Install Liquibase CLI first
brew install liquibase  # macOS
# or
apt install liquibase   # Ubuntu

# Rollback last changeset
liquibase rollback-count 1

# Rollback to specific changeset
liquibase rollback --tag=v1-baseline

# Preview rollback SQL (without executing)
liquibase rollback-count-sql 1
```

**Method 2: Manual SQL rollback**

```bash
# Step 1: Rollback V2 (seed data)
sudo -u postgres psql -d test_hemis \
  -f domain/src/main/resources/db/changelog/changesets/02-initial-data-rollback.sql

# Step 2: Rollback V1 (structure)
sudo -u postgres psql -d test_hemis \
  -f domain/src/main/resources/db/changelog/changesets/01-baseline-rollback.sql
```

### Migration Status

```bash
# Check applied migrations
liquibase status --verbose

# View migration history
liquibase history

# Validate changelog
liquibase validate
```

**SQL queries:**

```sql
-- Check migration history
SELECT id, author, filename, dateexecuted
FROM databasechangelog
ORDER BY orderexecuted;

-- Verify data
SELECT COUNT(*) FROM users;        -- Should be 1
SELECT COUNT(*) FROM roles;        -- Should be 5
SELECT COUNT(*) FROM permissions;  -- Should be 30
```

### What Gets Created

**V1 (Baseline - Database Structure):**
- ✅ 9 tables: users, roles, permissions, user_roles, role_permissions, languages, configurations, system_messages, message_translations
- ✅ 43 indexes for performance
- ✅ All constraints and foreign keys

**V2 (Initial Data - Seed):**
- ✅ 1 admin user
- ✅ 5 roles (SUPER_ADMIN, MINISTRY_ADMIN, UNIVERSITY_ADMIN, VIEWER, REPORT_VIEWER)
- ✅ 30 permissions
- ✅ 9 languages (uz-UZ, oz-UZ, ru-RU, en-US, etc.)
- ✅ 41 system messages + 164 translations

**Full documentation:** [scripts/LIQUIBASE_MIGRATION.md](scripts/LIQUIBASE_MIGRATION.md)

---

## 💻 Development

### Project Structure

```
hemis-back/
├── app/                           # Main application
│   ├── src/main/java/uz/hemis/app/
│   │   ├── HemisApplication.java  # Main class
│   │   └── config/                # App configuration
│   └── src/main/resources/
│       ├── application.yml        # Common config
│       ├── application-dev.yml    # Dev config
│       └── application-prod.yml   # Prod config
│
├── common/                        # Shared module
│   └── src/main/java/uz/hemis/common/
│       ├── dto/                   # Data Transfer Objects
│       ├── exception/             # Custom exceptions
│       └── util/                  # Utility classes
│
├── domain/                        # Data layer
│   └── src/main/java/uz/hemis/domain/
│       ├── entity/                # JPA entities
│       ├── repository/            # Spring Data repositories
│       └── mapper/                # MapStruct mappers
│
├── security/                      # Security layer
│   └── src/main/java/uz/hemis/security/
│       ├── config/                # Security config
│       ├── filter/                # JWT filters
│       └── service/               # Auth services
│
├── service/                       # Business logic
│   └── src/main/java/uz/hemis/service/
│       ├── StudentService.java
│       ├── TeacherService.java
│       └── ...
│
├── api-web/                       # Public REST APIs
│   └── src/main/java/uz/hemis/web/
│       ├── controller/            # REST controllers
│       └── dto/                   # API DTOs
│
└── docs/                          # Documentation
    ├── ARCHITECTURE_DECISION.md
    ├── API_CATEGORIZATION.md
    └── ...
```

### Building

```bash
# Build all modules
./gradlew clean build

# Build specific module
./gradlew :service:build

# Build without tests (faster)
./gradlew clean build -x test

# Refresh dependencies
./gradlew clean build --refresh-dependencies
```

### Testing

```bash
# Run all tests
./gradlew test

# Run specific module tests
./gradlew :service:test
./gradlew :api-web:test

# Run with coverage
./gradlew test jacocoTestReport

# View coverage report
open build/reports/jacoco/test/html/index.html
```

### Code Quality

```bash
# Checkstyle (code style)
./gradlew checkstyleMain

# SpotBugs (bug detection)
./gradlew spotbugsMain

# All quality checks
./gradlew check
```

### Hot Reload

Spring Boot DevTools enabled for hot reload:

```bash
# Run with DevTools
./gradlew :app:bootRun

# Edit code - changes auto-reload
```

---

## 📚 API Documentation

### Base URLs

| Environment | URL | Port |
|-------------|-----|------|
| **Development** | http://localhost:8081 | 8081 |
| **Production** | https://hemis.uz | 443 |

### API Prefix

All endpoints use: `/app/rest/v2`

### Authentication

**Login (OAuth2 compatible):**

```bash
POST /app/rest/v2/oauth/token
Content-Type: application/x-www-form-urlencoded
Authorization: Basic Y2xpZW50OnNlY3JldA==

grant_type=password
username=admin
password=Admin@123
```

**Response:**

```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 86400
}
```

**Use token in requests:**

```bash
curl -H "Authorization: Bearer <access_token>" \
  http://localhost:8081/app/rest/v2/students
```

### Key Endpoints

| Resource | Endpoint | Methods | Description |
|----------|----------|---------|-------------|
| **Students** | `/app/rest/v2/students` | GET, POST, PUT, DELETE | Student management |
| **Teachers** | `/app/rest/v2/teachers` | GET, POST, PUT, DELETE | Teacher profiles |
| **Departments** | `/app/rest/v2/departments` | GET, POST, PUT, DELETE | Department structure |
| **Diplomas** | `/app/rest/v2/diplomas` | GET, POST, PUT, DELETE | Diploma issuance |
| **Universities** | `/app/rest/v2/universities` | GET, POST, PUT, DELETE | University management |
| **Reports** | `/app/rest/v2/reports` | GET, POST | Reporting system |

### Interactive Documentation

**Swagger UI:** http://localhost:8081/swagger-ui/index.html

- ✅ All endpoints documented
- ✅ Try-it-out functionality
- ✅ Request/response examples
- ✅ Schema definitions

**OpenAPI JSON:** http://localhost:8081/v3/api-docs

---

## ⚙️ Configuration

### Application Profiles

| Profile | File | Purpose |
|---------|------|---------|
| **dev** | `application-dev.yml` | Local development |
| **prod** | `application-prod.yml` | Production |
| **test** | `application-test.yml` | Testing |

### Environment Variables

**Required:**

```bash
# Database
DB_MASTER_HOST=localhost
DB_MASTER_PORT=5432
DB_MASTER_NAME=test_hemis
DB_MASTER_USERNAME=postgres
DB_MASTER_PASSWORD=your-password

# JWT (OLD-HEMIS compatible - values in SECONDS)
JWT_SECRET=your-super-secret-key-minimum-64-characters-required
JWT_EXPIRATION=43200  # 12 hours in seconds (OLD-HEMIS: 43199)
JWT_REFRESH_EXPIRATION=604800  # 7 days in seconds

# Redis (optional)
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
```

**Optional:**

```bash
# Server
SERVER_PORT=8081

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_UZ_HEMIS=DEBUG

# Actuator
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,metrics,prometheus
```

### Configuration Files

**application-dev.yml:**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_MASTER_HOST:localhost}:${DB_MASTER_PORT:5432}/${DB_MASTER_NAME:test_hemis}
    username: ${DB_MASTER_USERNAME:postgres}
    password: ${DB_MASTER_PASSWORD:postgres}

  jpa:
    show-sql: true
    hibernate:
      ddl-auto: none

  liquibase:
    enabled: true
    change-log: classpath:/db/changelog/db.changelog-master.yaml

  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}

server:
  port: 8081

logging:
  level:
    root: INFO
    uz.hemis: DEBUG
```

---

## 🚢 Deployment

### Docker Deployment

**1. Build Docker image:**

```bash
# Using Gradle
./gradlew bootBuildImage

# Or using Dockerfile
docker build -t hemis-backend:2.1.0 .
```

**2. Run with Docker Compose:**

```yaml
# docker-compose.yml
version: '3.8'
services:
  postgres:
    image: postgres:18.0
    environment:
      POSTGRES_DB: test_hemis
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  app:
    image: hemis-backend:2.1.0
    depends_on:
      - postgres
      - redis
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_MASTER_HOST: postgres
      REDIS_HOST: redis
    ports:
      - "8081:8081"

volumes:
  postgres_data:
```

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop all services
docker-compose down
```

### Production Setup

**1. System requirements:**

- **CPU:** 4+ cores
- **RAM:** 8GB+ (16GB recommended)
- **Disk:** 50GB+ SSD
- **OS:** Ubuntu 22.04 LTS or RHEL 8+

**2. Install Java:**

```bash
# Amazon Corretto 21
wget https://corretto.aws/downloads/latest/amazon-corretto-21-x64-linux-jdk.tar.gz
tar -xzf amazon-corretto-21-x64-linux-jdk.tar.gz
sudo mv amazon-corretto-21.* /opt/corretto-21
echo 'export JAVA_HOME=/opt/corretto-21' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc
```

**3. Create systemd service:**

```ini
# /etc/systemd/system/hemis-backend.service
[Unit]
Description=HEMIS Backend Service
After=network.target postgresql.service

[Service]
Type=simple
User=hemis
WorkingDirectory=/opt/hemis-backend
ExecStart=/usr/bin/java -jar app-2.1.0.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

Environment="JAVA_HOME=/opt/corretto-21"
Environment="JAVA_OPTS=-Xms2g -Xmx4g -XX:+UseG1GC"

[Install]
WantedBy=multi-user.target
```

```bash
# Enable and start service
sudo systemctl daemon-reload
sudo systemctl enable hemis-backend
sudo systemctl start hemis-backend
sudo systemctl status hemis-backend
```

**4. Nginx reverse proxy:**

```nginx
# /etc/nginx/sites-available/hemis
upstream hemis_backend {
    server localhost:8081;
}

server {
    listen 80;
    server_name hemis.uz;

    # Redirect to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name hemis.uz;

    ssl_certificate /etc/letsencrypt/live/hemis.uz/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/hemis.uz/privkey.pem;

    location / {
        proxy_pass http://hemis_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

**5. SSL certificate:**

```bash
# Using Certbot
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d hemis.uz
```

---

## 📊 Monitoring

### Health Checks

**Application health:**

```bash
curl http://localhost:8081/actuator/health
```

**Response:**

```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" },
    "ping": { "status": "UP" },
    "redis": { "status": "UP" }
  }
}
```

### Metrics

**Prometheus metrics:**

```bash
curl http://localhost:8081/actuator/prometheus
```

**Key metrics:**

```
# JVM metrics
jvm_memory_used_bytes
jvm_threads_live_threads
jvm_gc_pause_seconds

# HTTP metrics
http_server_requests_seconds
http_server_requests_active

# Database metrics
hikaricp_connections_active
hikaricp_connections_idle

# Custom metrics
hemis_students_total
hemis_api_calls_total
```

### Logging

**Application logs:**

```bash
# Systemd journal
journalctl -u hemis-backend -f

# Or file-based
tail -f /var/log/hemis/application.log
```

**Log levels:**

```yaml
logging:
  level:
    root: INFO
    uz.hemis: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
  file:
    name: /var/log/hemis/application.log
    max-size: 100MB
    max-history: 30
```

---

## 🔧 Troubleshooting

### Common Issues

**1. Database connection failed**

```bash
# Check PostgreSQL is running
sudo systemctl status postgresql

# Test connection
psql -h localhost -U postgres -d test_hemis -c "SELECT 1;"

# Check credentials in .env
echo $DB_MASTER_PASSWORD
```

**2. Port 8081 already in use**

```bash
# Find process using port
sudo lsof -i :8081

# Kill process
sudo kill -9 <PID>

# Or change port in application-dev.yml
server.port: 8082
```

**3. Migration failed**

```bash
# Check migration status
liquibase status --verbose

# View error details
grep -i "liquibase\|error" logs/application.log

# Manual rollback if needed
liquibase rollback-count 1
```

**4. Out of memory**

```bash
# Increase heap size
export JAVA_OPTS="-Xms2g -Xmx4g"
./gradlew :app:bootRun

# Or in systemd service
Environment="JAVA_OPTS=-Xms2g -Xmx4g -XX:+UseG1GC"
```

**5. Redis connection failed**

```bash
# Check Redis is running
redis-cli ping  # Should return PONG

# Start Redis
sudo systemctl start redis

# Disable Redis temporarily
spring.cache.type: simple  # in application-dev.yml
```

### Debug Mode

```bash
# Run with debug logging
./gradlew :app:bootRun --args='--logging.level.uz.hemis=DEBUG'

# Enable SQL logging
spring.jpa.show-sql=true
logging.level.org.hibernate.SQL=DEBUG
```

### Health Checks

```bash
# Application health
curl http://localhost:8081/actuator/health | jq

# Database connectivity
curl http://localhost:8081/actuator/health/db | jq

# Disk space
curl http://localhost:8081/actuator/health/diskSpace | jq
```

---

## 🤝 Contributing

### Development Workflow

1. **Fork repository:**

```bash
git clone https://github.com/your-username/hemis-backend.git
cd hemis-backend
```

2. **Create feature branch:**

```bash
git checkout -b feature/your-feature-name
```

3. **Make changes and commit:**

```bash
git add .
git commit -m "feat: add student export functionality"
```

4. **Push and create PR:**

```bash
git push origin feature/your-feature-name
```

### Commit Message Convention

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add new feature
fix: bug fix
docs: documentation changes
style: code style changes
refactor: code refactoring
test: add or update tests
chore: maintenance tasks
```

**Examples:**

```
feat(student): add export to Excel functionality
fix(auth): resolve JWT token expiration issue
docs(readme): update deployment instructions
refactor(service): extract common logic to base class
```

### Code Style

- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use meaningful variable names
- Add JavaDoc for public methods
- Keep methods small and focused
- Write unit tests for new features

### Pull Request Checklist

- [ ] Code follows style guidelines
- [ ] Tests added/updated
- [ ] Documentation updated
- [ ] No breaking changes (or documented)
- [ ] CI/CD pipeline passes
- [ ] Reviewed by team member

---

## 📄 License

**Proprietary License**

Copyright © 2025 Ministry of Higher Education, Uzbekistan

This software is proprietary and confidential. Unauthorized copying, modification, distribution, or use of this software, via any medium, is strictly prohibited.

**Contact:**
- **Organization:** Ministry of Higher Education
- **Email:** support@hemis.uz
- **Website:** https://hemis.uz

---

## 📞 Support

### Documentation

- **Main README:** [README.md](README.md)
- **Migration Guide:** [scripts/LIQUIBASE_MIGRATION.md](scripts/LIQUIBASE_MIGRATION.md)
- **Architecture:** [docs/ARCHITECTURE_DECISION.md](docs/ARCHITECTURE_DECISION.md)
- **API Catalog:** [docs/API_CATEGORIZATION.md](docs/API_CATEGORIZATION.md)

### Getting Help

- **Issues:** https://github.com/hemis/hemis-backend/issues
- **Email:** support@hemis.uz
- **Slack:** #hemis-backend channel

### Team

- **Project Lead:** HEMIS Development Team
- **Architecture:** Multi-Module Monolith v2.1
- **Contributors:** See [CONTRIBUTORS.md](CONTRIBUTORS.md)

---

## 📝 Changelog

### [2.1.0] - 2025-11-12

**Migration Tool Upgrade:**
- ✅ Migrated from Flyway to Liquibase 4.31.1
- ✅ Native rollback support (Django/Rails-style)
- ✅ Changeset-based tracking with author + context
- ✅ Safety features: database checks, MD5 checksums, lock mechanism
- ✅ Complete documentation in README + migration guide

### [2.0.0] - 2025-10-30

**Initial Release:**
- ✅ Multi-module architecture implemented
- ✅ 10 controllers migrated to api-web module
- ✅ 10 services with business logic
- ✅ 100% backward compatibility with old-hemis
- ✅ OpenAPI 3.0 documentation
- ✅ JWT OAuth2 authentication

### [1.0.0] - Previous

**Legacy System:**
- Single-module CUBA Platform application
- 200+ API endpoints
- 289 database tables

---

## 🌟 Acknowledgments

Built with ❤️ by the HEMIS Development Team

**Powered by:**
- [Spring Boot](https://spring.io/projects/spring-boot)
- [PostgreSQL](https://www.postgresql.org/)
- [Liquibase](https://www.liquibase.org/)
- [Redis](https://redis.io/)

---

**Last Updated:** 2025-11-12
**Version:** v2.1.0
**Status:** 🟢 Production Ready
