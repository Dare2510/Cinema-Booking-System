package com.dare.cinema_booking_system.reservations.exceptions;

public class ReservationCancelNotOnTimeException extends RuntimeException {
	public ReservationCancelNotOnTimeException(Long reservationId) {
		super("Reservation with id " + reservationId + " cannot be cancelled." +
				"Reservation must be cancelled at least 60 min before screening");
	}
}
