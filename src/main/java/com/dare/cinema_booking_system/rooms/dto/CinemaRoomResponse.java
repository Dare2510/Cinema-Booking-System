package com.dare.cinema_booking_system.rooms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CinemaRoomResponse {

	private Long id;
	private int roomNumber;
	private int roomCapacity;
}
