# Dotask API

![Status](https://img.shields.io/badge/status-in%20development-brightgreen)
![License](https://img.shields.io/badge/license-MIT-blue)
![Java](https://img.shields.io/badge/java-21-red)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-green)

A comprehensive API for personal task and project management with complete CRUD functionality, authentication, and authorization.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technologies](#technologies)
- [Project Structure](#project-structure)
- [Endpoints](#endpoints)
- [Running the Project](#running-the-project)
  - [Requirements](#requirements)
  - [Setup](#setup)
  - [Running the Application](#running-the-application)
- [Tests](#tests)
- [API Documentation](#api-documentation)
- [Contributing](#contributing)
- [License](#license)

## 🔎 Overview

Dotask is a full-featured REST API for task and project management, allowing users to organize their daily activities. The API provides resources to create, list, update, and delete tasks and projects, as well as filter tasks by various criteria such as status, priority, due date, and project association.

## ✨ Features

### Users

- User registration
- JWT-based authentication
- Password recovery via email
- Email verification
- Profile management

### Projects

- Project creation with title, description, and custom color
- Project listing and filtering
- Project updates and deletion
- Automatic task counting per project

### Tasks

- Task creation with or without project association
- Task statuses (PENDING, IN_PROGRESS, COMPLETED, CANCELLED)
- Priority levels (LOW, MEDIUM, HIGH)
- Task filtering by multiple criteria
- Due dates
- Individual status updates
- Single or batch task deletion

## 🛠 Technologies

- **Java 21**
- **Spring Boot 3.4.5**
- **Spring Security** - For authentication and authorization
- **Spring Data JPA** - For data persistence
- **Flyway** - For database migrations
- **JWT** - For authentication token generation
- **PostgreSQL** - As the primary database
- **Maven** - For dependency management
- **JUnit 5** - For unit and integration testing
- **RestAssured** - For API testing
- **Swagger/OpenAPI** - For API documentation

## 🚀 Endpoints

### Authentication

- `POST /api/v1/token` - Request authentication token
- `POST /api/v1/token/refresh` - Refresh token using refresh token
- `POST /api/v1/token/revoke` - Invalidate a token

### Users

- `POST /api/v1/users` - Create a new user
- `GET /api/v1/users/me` - Get authenticated user data
- `PUT /api/v1/users/me` - Update user data
- `POST /api/v1/users/send-action-email` - Send email for verification or password reset
- `POST /api/v1/users/password/reset` - Reset password using token

### Projects

- `GET /api/v1/projects` - List all projects
- `GET /api/v1/projects/{id}` - Get project by ID
- `POST /api/v1/projects` - Create a new project
- `PUT /api/v1/projects/{id}` - Update project
- `DELETE /api/v1/projects/{id}` - Delete project
- `DELETE /api/v1/projects` - Delete multiple projects

### Tasks

- `GET /api/v1/tasks` - List all tasks
- `GET /api/v1/tasks/{id}` - Get task by ID
- `POST /api/v1/tasks` - Create a new task
- `PUT /api/v1/tasks/{id}` - Update task
- `DELETE /api/v1/tasks/{id}` - Delete task
- `DELETE /api/v1/tasks` - Delete multiple tasks
- `PATCH /api/v1/tasks/{id}/status` - Update task status

### Project Tasks

- `GET /api/v1/projects/{projectId}/tasks` - List tasks for a project
- `POST /api/v1/projects/{projectId}/tasks` - Create task in a project

## 🚦 Running the Project

### Requirements

- Java 21 or higher
- Maven 3.6 or higher
- PostgreSQL 12 or higher
- Docker (optional)

### Setup

1. Clone the repository:

   ```bash
   git clone https://github.com/andrehsvictor/Dotask-API.git
   cd dotask-api
   ```

2. Configure your database and other environment variables in `application.yml` or through environment variables.

3. Generate JWT keys (if not present):
   ```bash
   ./generate-keys.sh
   ```

### Running the Application

#### With Docker:

```bash
docker-compose -f docker-compose.dev.yml up -d
```

#### With Maven:

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

## 🧪 Tests

To run the tests:

```bash
./mvnw test          # Unit tests
./mvnw verify        # Unit + integration tests
```

## 📖 API Documentation

API documentation is available through Swagger UI when the application is running:

```
http://localhost:8080/swagger-ui.html
```

## 🤝 Contributing

Contributions are welcome! Feel free to open issues and pull requests.

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.
