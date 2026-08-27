package com.dare.cinema_booking_system.user.exception;

public class UserEmailAlreadyInUseException extends RuntimeException {

	public UserEmailAlreadyInUseException(String email) {
		super("User with email " + email + " already exists");
	}
}
