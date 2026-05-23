package com.dare.cinema_booking_system.reservations.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ReservationsRequest {

	@NotNull
	@Min(value = 1, message = "Screening id must be at least 1")
	private Long screeningId;

	@NotNull
	@Min(value = 1, message = "Screening seat id must be at least 1")
	private Long screeningSeatId;

}
