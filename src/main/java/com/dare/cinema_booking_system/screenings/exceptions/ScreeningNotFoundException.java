package com.dare.cinema_booking_system.screenings.exceptions;

public class ScreeningNotFoundException extends RuntimeException {
	public ScreeningNotFoundException(Long screeningId) {
		super("Screening with id " + screeningId + " not found");
	}
}
