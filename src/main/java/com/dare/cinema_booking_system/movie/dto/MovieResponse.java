package com.dare.cinema_booking_system.movie.dto;

import com.dare.cinema_booking_system.movie.entity.Genre;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieResponse {

	private Long id;
	private String title;
	private String description;
	private int duration;
	private Genre genre;
}
