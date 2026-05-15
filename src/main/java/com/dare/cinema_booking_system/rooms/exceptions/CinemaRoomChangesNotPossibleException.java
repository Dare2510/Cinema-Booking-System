package com.dare.cinema_booking_system.rooms.exceptions;

public class CinemaRoomChangesNotPossibleException extends RuntimeException {
	public CinemaRoomChangesNotPossibleException(Long cinemaRoomId) {
		super("Changes are not possible for cinema room with id " + cinemaRoomId + " screening exits");
	}
}
