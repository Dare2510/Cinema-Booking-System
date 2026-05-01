package com.dare.cinema_booking_system.movies.dto;

import com.dare.cinema_booking_system.movies.entity.Genre;
import lombok.*;

@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MoviesResponse {

	public Long id;
	public String title;
	public String description;
	public int duration;
	public Genre genre;
}
