# Cinema Booking Backend

## Overview

This project is a backend application for a cinema booking system.
It is built to manage movies, screenings, cinema rooms, seats, reservations, tickets, payments, and user authentication.

The goal of this project is to design and implement a structured backend system instead of coding features without a clear domain model.

## Version 1 Scope

Version 1 focuses on the core booking flow:

* user authentication with JWT
* movie management
* screening management
* cinema room and seat management
* reservation creation and cancellation
* ticket creation
* payment handling

Out of scope for version 1:

* frontend application
* email notifications
* refunds
* advanced reporting
* multi-cinema support
* external payment provider integration

## Main Features

* login with JWT
* create and manage screenings
* browse movies and screenings
* create reservations
* cancel reservations
* assign tickets to reserved screening seats
* store payment information

## Domain Model

Main entities in the system:

* User
* Movie
* Screening
* CinemaRoom
* Seat
* Reservation
* Ticket
* Payment
* ScreeningSeat

## Technology Stack

* Java
* Spring Boot
* Spring Security
* JWT
* JPA / Hibernate
* PostgreSQL
* Maven
* PlantUML

## Documentation

Project planning currently includes:

* use case diagrams
* class diagram
* sequence diagrams
* ER diagram

These diagrams are stored in the `docs/uml` folder.

## Current Status

The project is currently in the planning and design phase.

Finished so far:

* project scope definition
* domain object analysis
* class diagram draft
* sequence diagrams for core flows
* ER diagram draft
* define API endpoints

Next steps:

* create JPA entities
* design database migrations
* implement authentication
* implement reservation flow

## Project Goal

This project is primarily meant to improve backend design skills, domain modeling, and structured project planning.
