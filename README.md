# Cinema Booking System (Spring Boot Backend)

A Spring Boot backend application for cinema booking operations.

## Features

### Main Management operations

- Movie management
- Cinema room and seat management
- Screening management
- Reservation management (including ticket and payment)
- User management (first admin user is created on first start up)

### Main Customer operations
- User creation and manage own User(automated to be role of USER)
- Check available seats for specific screenings
- Reservation creation and cancellation

## Tech Stack

- Java
- Spring Boot
- Spring Data JPA / Hibernate
- PostgreSQL
- Maven
- JUnit / Mockito
- JWT Security 
- Swagger
- Docker

## Core Booking Flow

### Admin/Staff
  * creates a movie
  * creates a cinema room - seats are get automatically generated
  * creates a screening for a specific movie and room - seats for the screening are generated at screening creation.

### User
  * creates/ logs in
  * checks upcoming screening
  * checks free screening seats for the chosen screening
  * creates a reservation - selected seats get marked as reserved
  * pays on site or online

### Admin/Staff
 * marks the reservation as paid
 * marks ticket as used - when customer uses his ticket at the cinema 


## API Overview

### Customer / Reservation API

```http
POST /api/reservations
GET  /api/reservations/{reservationId}
POST /api/reservations/{reservationId}/cancel
```

### Management API

```http
GET   /api/management/reservations
GET   /api/management/reservations/{reservationId}
POST  /api/management/reservations/{reservationId}/cancel
PATCH /api/management/reservations/{reservationId}/payment/complete
PATCH /api/management/reservations/{reservationId}/refund/complete
PATCH /api/management/reservations/tickets/{ticketNumber}/used
```

### Movie, Screening and Room APIs

The project also contains endpoints for managing movies, cinema rooms, seats and screenings.
These endpoints are part of the backend administration flow and are be protected with role-based security.

## Example Reservation Request

```json
{
  "screeningId": 1,
  "cinemaRoomSeatIds": [1, 2],
  "paymentMethod": "ONLINE"
}
```

## Domain Notes

A physical seat belongs to a cinema room.  

A screening seat belongs to a specific screening and represents the availability of that physical seat for that screening.

This distinction is important because the same physical seat can be free in one screening and reserved in another.

Reservations are linked to their reserved screening seats through a join table, so cancellation only affects the seats that belong to the canceled reservation.

Every user can only manage his own reservations and user.

## Architecture

The application follows a layered architecture:

`Controller -> Security -> Service -> Repository -> Database`

### Additional Components

- GlobalExceptionHandler
- ErrorResponse
- JWT filter
- Custom domain exceptions
- Jakarta Bean Validation

## Security

Authentication is handled using JWT tokens.

### Roles

- `USER` – can create and manage his own user, create manage his reservations  
- `STAFF` – can manage movies, rooms, screenings, reservations and users with restrictions. 
- `ADMIN` – full system access

## Database

The application uses PostgreSQL with:

- `spring.jpa.hibernate.ddl-auto=update`

## Configuration

The application uses the following environment variables:

- `DB_HOST`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`

- `JWT_SECRET`
- `JWT_EXPIRATION_MS`

- `ADMIN_EMAIL`
- `ADMIN_PASSWORD`


### Example `.env`

```env
DB_HOST=postgres
DB_NAME=cinema
DB_USERNAME=cinema
DB_PASSWORD=cinema

JWT_SECRET=replace-with-a-long-secret
JWT_EXPIRATION_MS=3600000

ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD=change-me-now
```

**Running locally**

Make sure PostgreSQL is running and that a database named cinema exists.

Create a .env file based on .env.example, then run:
```
./mvnw spring-boot:run
```
**Running with Docker Compose**

From the docker directory run:
```
docker compose --env-file ../.env up --build
```
This starts:

PostgreSQL mapped to host port 54431
Spring Boot application mapped to host port 8080

The application connects to PostgreSQL internally via:

```jdbc:postgresql://postgres:5432/cinema```

## Testing

The project includes:
* integration tests using MockMvc
* service layer test
* security tests

**Run tests with:**
```
./mvnw test
```

The goal is to keep the scope focused on backend development.

## Documentation

UML diagrams are included in the project documentation folder.

