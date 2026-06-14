package com.dare.cinema_booking_system.user.exception;

public class UserDoubleCreationException extends RuntimeException {
	public UserDoubleCreationException(String email) {

		super("User with " + email + " already exists");
	}
}
