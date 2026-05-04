package com.dare.cinema_booking_system.rooms.exceptions;

public class CinemaRoomNotFoundException extends RuntimeException {
	public CinemaRoomNotFoundException(Long roomId) {
		super("Cinema Room with ID " + roomId + " not found");
	}
}
