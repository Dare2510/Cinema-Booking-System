package com.dare.cinema_booking_system.user.exception;

public class UserDeletionNotPossibleException extends RuntimeException {

	public UserDeletionNotPossibleException(String email, Long userId) {
		super("User with id " + userId + " and email " + email + " has open reservations, deletion not possible");
	}

	public UserDeletionNotPossibleException() {
		super("Deletion not possible, you have open reservations");
	}
}
