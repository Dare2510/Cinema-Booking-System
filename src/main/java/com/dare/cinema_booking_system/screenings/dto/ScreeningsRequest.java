package com.dare.cinema_booking_system.screenings.dto;

import com.dare.cinema_booking_system.screenings.entity.TimeSlot;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class ScreeningsRequest {

	@NotNull(message = "Room id is required")
	@Min(value = 1, message = "Room id must be at least 1")
	private Long roomId;

	@NotNull(message = "Movie id is required")
	@Min(value = 1, message = "Movie id must be at least 1")
	private Long movieId;

	@NotNull(message = "Screening date is required")
	@PastOrPresent(message = "Screening date must be in the future")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate screeningDate;

	@NotNull(message = "Timeslot is required, available Timeslots are: EVENING,PRIME,NIGHT")
	private TimeSlot timeSlot;

	@NotNull(message = "Price is required")
	@Min(value = 1, message = "Price must be at least 1")
	private BigDecimal price;
}
