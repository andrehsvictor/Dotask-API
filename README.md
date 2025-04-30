# Dotask API 🚀

[![CI/CD Pipeline](https://github.com/andrehsvictor/Dotask-API/actions/workflows/main.yml/badge.svg)](https://github.com/andrehsvictor/Dotask-API/actions/workflows/main.yml)

A modern, RESTful API for task management built with Spring Boot. Dotask allows users to create, organize, and track tasks with support for projects, priorities, and status tracking.

## 🌟 Features

- **User Management**: Registration, email verification, and profile management
- **Authentication**: Secure JWT-based authentication with access and refresh tokens
- **Task Management**: Create, update, and track tasks with various attributes:
  - Priority levels (Low, Medium, High)
  - Status tracking (Pending, In Progress, Completed, Cancelled)
  - Due dates and descriptions
- **Project Organization**: Group related tasks under projects
- **Advanced Filtering**: Filter tasks by status, priority, due date, and more
- **Search Functionality**: Search through tasks and projects
- **API Documentation**: Comprehensive OpenAPI documentation

## 🔧 Tech Stack

- **Java 21** with Spring Boot
- **Spring Security** with JWT authentication
- **PostgreSQL** database with Spring Data JPA
- **Docker** containerization
- **Swagger/OpenAPI** for API documentation
- **RESTful API** design principles
- **GitHub Actions** for CI/CD

## 📋 API Endpoints

### Authentication

- `POST /api/v1/token`: Request authentication tokens
- `POST /api/v1/token/refresh`: Refresh access token
- `POST /api/v1/token/revoke`: Revoke a token

### User Management

- `POST /api/v1/users`: Create a new user
- `GET /api/v1/users/me`: Get authenticated user data
- `PUT /api/v1/users/me`: Update user data
- `POST /api/v1/users/email/verify`: Verify email address
- `POST /api/v1/users/password/reset`: Reset password
- `POST /api/v1/users/send-action-email`: Send action emails (verification, password reset)

### Projects

- `GET /api/v1/projects`: Find all projects
- `GET /api/v1/projects/{id}`: Find project by ID
- `POST /api/v1/projects`: Create a new project
- `PUT /api/v1/projects/{id}`: Update project
- `DELETE /api/v1/projects/{id}`: Delete project
- `DELETE /api/v1/projects`: Delete multiple projects

### Tasks

- `GET /api/v1/tasks`: Find all tasks
- `GET /api/v1/tasks/{id}`: Find task by ID
- `POST /api/v1/tasks`: Create a new task
- `PUT /api/v1/tasks/{id}`: Update task
- `PATCH /api/v1/tasks/{id}/status`: Update task status
- `DELETE /api/v1/tasks/{id}`: Delete task
- `DELETE /api/v1/tasks`: Delete multiple tasks
- `GET /api/v1/projects/{projectId}/tasks`: Find tasks by project
- `POST /api/v1/projects/{projectId}/tasks`: Create a task in a project

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Docker and Docker Compose
- Maven 3.8+

### Running Locally

1. **Clone the repository:**

```bash
git clone https://github.com/andrehsvictor/Dotask-API.git
cd Dotask-API
```

2. **Generate RSA key pair:**

```bash
chmod +x generate-keys.sh
./generate-keys.sh
```

3. **Configure .env file:**

Copy the `.env.example` file to `.env` and update the values as needed.

```bash
cp .env.example .env
```

4. **Run the application:**

```bash
./mvnw spring-boot:run
```

5. **Access the API:**

Open your browser and navigate to `http://localhost:8080/swagger-ui.html` to view the API documentation and test endpoints.