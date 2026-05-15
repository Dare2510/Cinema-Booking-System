package com.dare.cinema_booking_system.rooms.exceptions;

public class CinemaRoomNumberDuplicateException extends RuntimeException {
	public CinemaRoomNumberDuplicateException(int roomNumber) {
		super("Room number " + roomNumber + " is already in use, " +
				"please choose another room number");
	}
}
