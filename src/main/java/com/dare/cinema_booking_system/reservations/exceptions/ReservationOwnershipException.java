package com.dare.cinema_booking_system.reservations.exceptions;

public class ReservationOwnershipException extends RuntimeException {
	public ReservationOwnershipException(Long reservationId) {

		super("You don't own the reservation with id " + reservationId);
	}
}
