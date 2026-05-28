package com.dare.cinema_booking_system.reservations.exceptions;

public class ReservationRefundException extends RuntimeException {
	public ReservationRefundException(Long reservationId) {
		super("Reservation with " + reservationId +" id cannot be refunded. Check status of reservation and payment");
	}
}
