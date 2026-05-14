package com.dare.cinema_booking_system.screenings.dto;

import com.dare.cinema_booking_system.screenings.entity.TimeSlot;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
public class ScreeningsResponse {

	private Long id;
	private Long movieId;
	private TimeSlot timeSlot;
	private LocalDate screeningDate;

}
