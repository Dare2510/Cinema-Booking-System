package com.dare.cinema_booking_system.rooms.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CinemaRoomRequest {

	@NotNull
	@Size(min = 1, message = "Set an custom room number, starting with 1")
	private int roomNumber;

	@NotNull
	@Size(min = 10, max = 30, message = "Minimum rows = 10, Maximum rows = 30")
	private int rows;

	@NotNull
	@Size(min = 10, max = 50, message = "Minimum capacity per row = 10," +
			" Maximum capacity per row = 30")
	private int rowCapacity;
}
