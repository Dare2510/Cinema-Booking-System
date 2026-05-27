package com.dare.cinema_booking_system.reservations.exceptions;

import com.dare.cinema_booking_system.reservations.entity.ReservationStatus;

public class ReservationInvalidStatusFlowException extends RuntimeException {
	public ReservationInvalidStatusFlowException(Long reservationId, ReservationStatus reservationStatus) {
		super("Status of reservation with " + reservationId + " cannot be changed to " + reservationStatus.toString()
				+ "invalid order. ");
	}
}
