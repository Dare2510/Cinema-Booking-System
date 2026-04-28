package com.dare.cinema_booking_system.movies.dto;

import com.dare.cinema_booking_system.movies.entity.Genre;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class MoviesResponse {

	public Long id;
	public String title;
	public String description;
	public Integer duration;
	public Genre genre;
}
