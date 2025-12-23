# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a full-stack web application for the apparel industry talent certification and recruitment platform. It integrates skill certification, recruitment services, and training recommendations into a vertical platform specifically designed for the clothing industry.

**Technology Stack:**
- **Backend**: Spring Boot 2.7.15 (Java 8) + MyBatis-Plus + MySQL 8.0
- **Frontend**: Vue.js 3.3.4 + Vite + Element Plus
- **Architecture**: Frontend-backend separation with RESTful API

## Development Commands

### Backend (Java/Spring Boot)

```bash
cd backend

# Build and run
mvn clean install
mvn spring-boot:run

# Or run from IDE
# Main class: com.apparelcert.ApparelCertApplication
# Server runs on port 8080
```

### Frontend (Vue.js)

```bash
cd frontend

# Install dependencies
npm install

# Development server (port 3000)
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

### Convenience Scripts (Windows)

Located in project root:
- `start.bat` - Start frontend development server (auto-kills port 3000)
- `build.bat` - Build frontend for production
- `status.bat` - Check service status
- `stop.bat` - Stop running services

### Database

```bash
# Initialize database
mysql -u root -p < database/create_tables.sql
```

Database configuration: [backend/src/main/resources/application.yml](backend/src/main/resources/application.yml)

## Project Architecture

### Directory Structure

```
apparel-cert-frame/
├── backend/src/main/java/com/apparelcert/
│   ├── common/          # Common utilities, constants, enums
│   ├── config/          # Spring configuration (Security, CORS, Swagger)
│   ├── controller/      # REST API endpoints
│   ├── entity/          # J entities (database tables)
│   ├── mapper/          # MyBatis-Plus mappers
│   ├── service/         # Business logic layer
│   └── utils/           # Utility classes (JWT, password hashing)
├── frontend/src/
│   ├── router/          # Vue Router configuration
│   ├── views/           # Page components organized by user role
│   │   ├── admin/       # Admin dashboard
│   │   ├── enterprise/  # Enterprise user interface
│   │   └── personal/    # Personal user interface
│   └── main.js
└── database/
    └── create_tables.sql
```

### User Roles

The system supports four distinct user types:
1. **Personal Users (求职者)** - Job seekers with resumes
2. **Enterprise Users (招聘方)** - Companies posting jobs
3. **Experts (评审专家)** - Review certification applications
4. **Administrators** - System management

### Key Design Patterns

**Separate User Tables**: Personal users ([user_personal](database/create_tables.sql#L6)), enterprise users ([user_enterprise](database/create_tables.sql#L23)), and experts ([expert](database/create_tables.sql#L43)) are stored in separate tables rather than a single user table with role differentiation.

**JSON Storage for Flexible Data**: The [resume table](database/create_tables.sql#L114) uses JSON fields for education, work experience, projects, skills, and certificates to allow flexible schema evolution.

**Certification Workflow**: The certification system involves:
1. User applies for certification ([certification_application](database/create_tables.sql#L72))
2. Takes online theory exam ([question_bank](database/create_tables.sql#L100))
3. Uploads practical work file
4. Expert reviews and approves
5. Certificate generated ([certification_certificate](database/create_tables.sql#L89))

## API Architecture

**Base URL**: `http://localhost:8080/api`

**Authentication**: JWT token-based authentication. Include token in Authorization header:
```
Authorization: Bearer <token>
```

**Key API Endpoints**:
- `/api/auth/*` - Authentication (login, register, password reset)
- `/api/resume/*` - Resume management (CRUD, export PDF)
- `/api/certification/*` - Skill certification workflow
- `/api/job/*` - Job posting and search
- `/api/application/*` - Job application tracking
- `/api/interview/*` - Interview management
- `/api/admin/*` - System administration

**API Documentation**: Swagger UI available at `http://localhost:8080/swagger-ui.html`

## Frontend Configuration

**API Proxy**: Vite proxy configured to forward `/api` requests to `http://localhost:8080/api`

**Vue Router**: Role-based routing structure:
- `/personal/*` - Personal user pages
- `/enterprise/*` - Enterprise user pages
- `/admin/*` - Admin pages

## Database Configuration

**Connection**: Modify in [backend/src/main/resources/application.yml](backend/src/main/resources/application.yml#L7)

**Character Set**: utf8mb4 with utf8mb4_unicode_ci collation

**Important Tables**:
- Certification tables: `certification_standard`, `certification_application`, `certification_certificate`
- Recruitment tables: `job_position`, `resume_delivery`, `interview`
- User tables: `user_personal`, `user_enterprise`, `expert`

## Security Implementation

**Password Encryption**: BCrypt hashing (see [AuthServiceImpl.java](backend/src/main/java/com/apparelcert/service/impl/AuthServiceImpl.java))

**JWT Authentication**: Token generation and validation in [JwtUtil.java](backend/src/main/java/com/apparelcert/utils/JwtUtil.java)

**Spring Security Config**: [SecurityConfig.java](backend/src/main/java/com/apparelcert/config/SecurityConfig.java) - configure which endpoints are public vs authenticated

## File Upload

**Configuration**: Max file size 10MB configured in [application.yml](backend/src/main/resources/application.yml#L30)

**Upload Path**: Configurable via `apparelcert.upload.path`

**Supported Formats** (for certification practical work):
- Design works: JPG, PNG
- Pattern files: CAD, PDF

## Current Development Status

Overall progress: **48%** (see [开发任务文档.md](开发任务文档.md) for detailed tracking)

**Completed**:
- Basic authentication (registration, login, JWT)
- Resume CRUD operations
- Certification application flow
- Job posting and search
- Basic application tracking

**In Progress**:
- Online examination system (auto-generation, scoring)
- File upload validation
- Interview management
- Notification system

**Not Started**:
- Certificate PDF generation
- Email service (password recovery)
- Training course recommendation
- Smart job recommendation algorithm

## Code Conventions

**Java**: Follow Alibaba Java Coding Guidelines
- Controller layer: Parameter validation and request forwarding only
- Service layer: Business logic implementation
- Unified exception handling and response format

**Git Commit Format**:
- `feat:` - New feature
- `fix:` - Bug fix
- `docs:` - Documentation update
- `refactor:` - Code refactoring
- `style:` - Code formatting
- `test:` - Test related
- `chore:` - Build/tools changes

## Important Files

- [application.yml](backend/src/main/resources/application.yml) - All backend configuration
- [create_tables.sql](database/create_tables.sql) - Complete database schema
- [开发任务文档.md](开发任务文档.md) - Detailed task tracking with 407 lines
- [需求文档(1).md](需求文档(1).md) - Requirements specification (Chinese)
