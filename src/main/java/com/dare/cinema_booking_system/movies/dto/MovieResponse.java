package com.dare.cinema_booking_system.movies.dto;

import com.dare.cinema_booking_system.movies.entity.Genre;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieResponse {

	public Long id;
	public String title;
	public String description;
	public int duration;
	public Genre genre;
}
