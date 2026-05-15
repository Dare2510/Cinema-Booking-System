package com.dare.cinema_booking_system.rooms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CinemaRoomResponse {

	private Long id;
	private int roomNumber;
	private int capacity;
}
