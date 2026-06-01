package com.dare.cinema_booking_system.screenings.dto;

import com.dare.cinema_booking_system.movie.dto.MovieResponse;
import com.dare.cinema_booking_system.rooms.dto.CinemaRoomResponse;
import com.dare.cinema_booking_system.screenings.entity.TimeSlot;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScreeningResponse {

	private Long id;
	private CinemaRoomResponse cinemaRoomInformation;
	private MovieResponse movieInformation;
	private TimeSlot timeSlot;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
	private BigDecimal price;
	private LocalDate screeningDate;

}
