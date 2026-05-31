package com.dare.cinema_booking_system.screenings.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@Builder
public class ScreeningSeatResponse {

	private Integer seatNumber;
	private Integer rowNumber;
	private Long cinemaRoomSeatId;
}
