package com.dare.cinema_booking_system.screenings.exceptions;

public class ScreeningUpdateNotPossibleException extends RuntimeException {
	public ScreeningUpdateNotPossibleException(Long screeningId) {
		super("Screening with id " + screeningId + " cannot be updated");
	}
}
