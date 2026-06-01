package com.dare.cinema_booking_system.screenings.exceptions;

public class ScreeningSeatNotAvailableException extends RuntimeException {
	public ScreeningSeatNotAvailableException(Long screeningSeatId) {
		super("Seat " + screeningSeatId + " is not found or available");
	}

	public ScreeningSeatNotAvailableException() {

		super("Chosen seats are not available");
	}
}
