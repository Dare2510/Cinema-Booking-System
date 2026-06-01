package com.dare.cinema_booking_system.reservations.exceptions;

public class ReservationCompletePaymentException extends RuntimeException {
	public ReservationCompletePaymentException(Long reservationId) {
		super("Payment for reservation with " + reservationId + " id cannot be completed." +
				" Check status of reservation and payment");
	}
}
