package com.dare.cinema_booking_system.reservations.exceptions;

public class TicketUseNotPossibleException extends RuntimeException {
	public TicketUseNotPossibleException(String ticketNumber) {
		super("Ticket number " + ticketNumber + " cannot check in, check status");
	}
}
