# Gemini Project Context: 服装行业人才技能认证与招聘服务平台

## 1. Project Overview

This is a full-stack web application designed as a vertical talent service platform for the apparel industry. It aims to connect skilled professionals with employers by integrating skill certification, recruitment services, and training recommendations.

The project follows a classic monolithic backend with a separated single-page application (SPA) frontend.

**Key Technologies:**

*   **Backend:**
    *   **Framework:** Spring Boot 2.7.15
    *   **Language:** Java 8
    *   **Database:** MySQL 8.0 with MyBatis-Plus for ORM
    *   **Authentication:** Spring Security with JWT
    *   **Build Tool:** Maven
    *   **API Documentation:** Swagger 3 (Knife4j)

*   **Frontend:**
    *   **Framework:** Vue.js 3.3.4
    *   **Build Tool:** Vite 4.4.9
    *   **UI Library:** Element Plus 2.3.12
    *   **Routing:** Vue Router 4.2.4
    *   **HTTP Client:** Axios

**Architecture:**

*   The backend is located in the `backend/` directory and follows a standard layered architecture: `Controller` -> `Service` -> `Mapper` -> `Entity`.
*   The frontend is in the `frontend/` directory, structured with a clear separation of `views` for different user roles (admin, enterprise, personal), a central router, and utility modules.
*   Database creation scripts are in the `database/` directory.

## 2. Building and Running

### Environment Prerequisites

*   JDK 8+
*   Node.js 16+
*   MySQL 8.0+
*   Maven 3.6+

### Step 1: Database Setup

1.  Ensure your MySQL server is running.
2.  Import the database schema using the provided script. You may need to create the database first.
    ```sh
    # Example: mysql -u root -p -e "CREATE DATABASE apparel_cert;"
    mysql -u root -p apparel_cert < database/create_tables.sql
    ```
3.  Update the database connection details in `backend/src/main/resources/application.yml`:
    ```yaml
    spring:
      datasource:
        url: jdbc:mysql://localhost:3306/apparel_cert
        username: your_username
        password: your_password
    ```

### Step 2: Run the Backend

1.  Navigate to the backend directory:
    ```sh
    cd backend
    ```
2.  Build the project and run the server using Maven:
    ```sh
    mvn clean install
    mvn spring-boot:run
    ```
3.  The backend API will be available at `http://localhost:8080`.
4.  API documentation can be accessed at `http://localhost:8080/swagger-ui.html`.

### Step 3: Run the Frontend

1.  Navigate to the frontend directory in a new terminal:
    ```sh
    cd frontend
    ```
2.  Install dependencies:
    ```sh
    npm install
    ```
3.  Start the development server:
    ```sh
    npm run dev
    ```
4.  The frontend application will be accessible at `http://localhost:3000`.

## 3. Development Conventions

*   **API Design:** The backend exposes a RESTful API with endpoints prefixed by `/api`. Refer to the Swagger documentation for detailed specifications.
*   **Authentication:** API requests are secured using JWT. The token must be included in the `Authorization` header of authenticated requests.
*   **Code Style:**
    *   **Java:** Standard Java conventions are expected. The code is structured into clear packages for configuration, controllers, entities, services, and mappers.
    *   **Vue:** Follows the standard Vue 3 `<script setup>` syntax and component-based architecture.
*   **Database Interaction:** All database operations are handled by the MyBatis-Plus framework. Mappers define the interfaces for database queries.
*   **Configuration:** All backend configuration is centralized in `application.yml`. This includes database credentials, JWT secrets, and server ports.
