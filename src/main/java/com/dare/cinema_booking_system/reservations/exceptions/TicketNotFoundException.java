package com.dare.cinema_booking_system.reservations.exceptions;

public class TicketNotFoundException extends RuntimeException {
	public TicketNotFoundException(String ticketNumber) {

		super("Ticket number " + ticketNumber + " not found");
	}

	public TicketNotFoundException(Long ticketId) {

		super("Ticket with id " + ticketId + " not found");
	}
}
