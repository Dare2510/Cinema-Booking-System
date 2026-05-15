package com.dare.cinema_booking_system.screenings.exceptions;

import com.dare.cinema_booking_system.screenings.entity.TimeSlot;

import java.time.LocalDate;

public class ScreeningSlotAlreadyBookedException extends RuntimeException {
	public ScreeningSlotAlreadyBookedException(Long roomId, LocalDate screeningDate, TimeSlot timeSlot) {

		super("Screening slot in " + roomId + " room ID and " + screeningDate + " and timeslot " + timeSlot + " already booked");
	}
}
