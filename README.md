# Cinema Booking System

Backend-only project built with **Java** and **Spring Boot**.

The application models a cinema booking flow with movies, cinema rooms, seats, screenings, reservations, tickets and an internal payment status flow. The main focus is backend architecture, domain modeling, reservation consistency, validation, testing and security.

## Tech Stack

- Java
- Spring Boot
- Spring Data JPA / Hibernate
- PostgreSQL
- Maven
- JUnit / Mockito
- JWT Security planned
- Docker planned

## Features

- Movie management
- Cinema room and seat management
- Screening management
- Seat availability per screening
- Reservation creation and cancellation
- Ticket generation and ticket validation
- Internal payment and refund status flow
- Customer and management API structure

## Core Booking Flow

1. A screening is created for a movie and a cinema room.
2. Seats for that screening are generated as screening seats.
3. A customer checks the free seats for a screening.
4. A customer creates a reservation with selected cinema room seat IDs.
5. The selected screening seats are marked as reserved.
6. A ticket and payment record are created for the reservation.
7. Staff or admin can complete payment, complete refund or validate tickets.

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
These endpoints are part of the backend administration flow and will be protected with role-based security.

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

Reservations are linked to their reserved screening seats through a join table, so cancellation only affects the seats that belong to the cancelled reservation.

## Current Status

Implemented:

- Core movie, room, screening and reservation logic
- Seat availability handling
- Reservation cancellation flow
- Ticket and payment status handling
- UML documentation
- Unit tests for reservation and status logic
- Integration tests for reservation endpoints

Planned before final portfolio version:

- JWT authentication
- Role-based authorization
- Docker setup with PostgreSQL

The goal is to keep the scope focused on backend development.

## Documentation

UML diagrams are included in the project documentation folder.

