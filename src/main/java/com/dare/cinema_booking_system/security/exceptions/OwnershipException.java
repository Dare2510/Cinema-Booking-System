package com.dare.cinema_booking_system.security.exceptions;

public class OwnershipException extends RuntimeException {
	public OwnershipException(Long reservationId) {

		super("You don't own the reservation with id " + reservationId);
	}
}
