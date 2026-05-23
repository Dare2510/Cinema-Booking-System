package com.dare.cinema_booking_system.reservations.exceptions;

public class ReservationNotFoundException extends RuntimeException {
	public ReservationNotFoundException(Long reservationId) {
		super("Reservation with id " + reservationId + " not found");
	}
}
