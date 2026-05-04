package com.dare.cinema_booking_system.rooms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;

@Getter
@AllArgsConstructor
public class CinemaRoomResponse {

	private final Long id;
	private final int roomNumber;
	private final int capacity;
}
