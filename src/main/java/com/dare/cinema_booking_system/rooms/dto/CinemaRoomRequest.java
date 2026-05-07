package com.dare.cinema_booking_system.rooms.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CinemaRoomRequest {

	@NotNull
	@Min(value = 1, message = "Room number must be at least 1 and unique")
	private int roomNumber;

	@NotNull
	@Min(value = 10, message = "Minium number of rows is 10")
	@Max(value = 30, message = "Maximum number of rows is 50")
	private int rows;

	@NotNull
	@Min(value = 10, message = "Capacity per row must be at least 10")
	@Max(value = 50, message = "Capacity per row cannot be more than 50")
	private int rowCapacity;
}
