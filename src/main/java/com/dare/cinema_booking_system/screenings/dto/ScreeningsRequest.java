package com.dare.cinema_booking_system.screenings.dto;

import com.dare.cinema_booking_system.screenings.entity.TimeSlot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
@AllArgsConstructor
public class ScreeningsRequest {

	private Long roomId;
	private Long movieId;
	private LocalDate screeningDate;
	private TimeSlot timeSlot;

}
