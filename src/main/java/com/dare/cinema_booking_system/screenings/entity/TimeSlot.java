package com.dare.cinema_booking_system.screenings.entity;

import lombok.Getter;

import java.time.LocalTime;

@Getter
public enum TimeSlot {
	EVENING(LocalTime.of(17, 0), LocalTime.of(19, 0)),
	PRIME(LocalTime.of(20, 0), LocalTime.of(22, 0)),
	NIGHT(LocalTime.of(23, 0), LocalTime.of(1, 0));

	private final LocalTime startTime;
	private final LocalTime endTime;

	TimeSlot(LocalTime startTime, LocalTime endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
	}

}
