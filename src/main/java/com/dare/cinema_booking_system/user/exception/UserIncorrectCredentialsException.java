package com.dare.cinema_booking_system.user.exception;

public class UserIncorrectCredentialsException extends RuntimeException {
	public UserIncorrectCredentialsException() {
		super("Invalid password");
	}
}
