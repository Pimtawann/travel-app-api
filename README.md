# Travel App - Backend API

A RESTful API for a travel management application built with Spring Boot and PostgreSQL (Supabase)

## Technology Stack

- **Java 21**
- **Spring Boot 3.5.7**
- **Spring Security** - Authentication & Authorization
- **Spring Data JPA** - ORM and Database Management
- **PostgreSQL** - Database (Supabase)
- **JWT (JSON Web Tokens)** - Token-based Authentication
- **Supabase Storage** - File/Image Upload
- **HikariCP** - Connection Pool Management
- **Lombok** - Code Generation
- **Maven** - Dependency Management

## Features

- ✅ User Authentication (Register/Login) with JWT
- ✅ User Profile Management
- ✅ Trips CRUD Operations
- ✅ Trip Search Functionality
- ✅ Image Upload with Supabase Storage
- ✅ Ownership-based Authorization
- ✅ CORS Configuration
- ✅ Connection Pool Optimization

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- PostgreSQL Database (or Supabase Account)
- Supabase Storage Bucket

## Getting Started

### 1. Clone Repository

```bash
git clone https://github.com/Pimtawann/travel-app-api.git
cd travel-app
```

### 2. Configuration

Create `application-local.properties` file in `src/main/resources/`:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://your-host:6543/postgres?sslmode=require&prepareThreshold=0
spring.datasource.username=your-username
spring.datasource.password=your-password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Supabase Storage
supabase.url=https://your-project.supabase.co
supabase.bucket=uploads
supabase.apiKey=your-supabase-api-key

# JWT Secret (generate your own secret key)
jwt.secret=your-jwt-secret-key
jwt.expiration=86400000
```

### 3. Build and Run

```bash
# Build project
mvn clean install

# Run application
mvn spring-boot:run
```

The application will run at: `http://localhost:8080`

## API Endpoints

### Authentication
- `POST /auth/register` - Register new user
- `POST /auth/login` - User login
- `GET /auth/me` - Get email from token (requires authentication)
- `GET /auth/profile` - Get user profile (requires authentication)
- `PUT /auth/profile` - Update user profile (requires authentication)

### Trips Management
- `GET /api/trips` - Get all trips
- `GET /api/trips?query=xxx` - Search trips
- `GET /api/trips/{id}` - Get trip by ID
- `GET /api/trips/mine` - Get my trips (requires authentication)
- `POST /api/trips` - Create new trip (requires authentication)
- `PUT /api/trips/{id}` - Update trip (requires authentication and ownership)
- `DELETE /api/trips/{id}` - Delete trip (requires authentication and ownership)

### File Upload
- `POST /api/files/upload` - Upload image (requires authentication)

## API Examples

### Register

```bash
POST /auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "displayName": "John Doe"
}
```

### Login

```bash
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Get Profile

```bash
GET /auth/profile
Authorization: Bearer <your-token>
```

### Create Trip

```bash
POST /api/trips
Authorization: Bearer <your-token>
Content-Type: application/json

{
  "title": "Trip to Tokyo",
  "description": "Amazing trip to Japan",
  "photos": "https://example.com/photo.jpg",
  "tags": "japan",
  "latitude": "35.3606",
  "longitude": "138.7274"
}
```

### Upload Image

```bash
POST /api/files/upload
Authorization: Bearer <your-token>
Content-Type: multipart/form-data

file: [binary-file-data]
```

## Database Schema

### Users Table
- `id` (Long, Primary Key)
- `email` (String, Unique)
- `password_hash` (String)
- `display_name` (String)
- `created_at` (Timestamp)

### Trips Table
- `id` (Long, Primary Key)
- `title` (String)
- `description` (Text)
- `latitude` (Float8)
- `longitude` (Float8)
- `tags` (Text)
- `photos` (String)
- `author_id` (Long, Foreign Key)
- `created_at` (Timestamp)
- `updated_at` (Timestamp)

## Security

- **JWT Authentication**: Uses JWT tokens for stateless authentication
- **Password Encryption**: Uses BCrypt for password hashing
- **CORS Configuration**: Supports origins:
  - `http://localhost:5173`
  - `http://localhost:5174`
  - `https://*.vercel.app`
  - `https://*.netlify.app`
- **Authorization**: Validates ownership for user-specific operations
