package com.dare.cinema_booking_system.movies.dto;

import com.dare.cinema_booking_system.movies.entity.Genre;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class MovieRequest {

	@NotBlank(message = "Title is required")
	@Size(min = 1, max = 100, message = "Title must have between 1 and 100 Characters")
	public String title;

	@NotBlank(message = "Description is required")
	@Size(min = 1, max = 200, message = "Description must have between 1 and 200 Characters")
	public String description;

	@NotNull
	@Min(value = 30, message = "Duration must be at least 30 min")
	public int duration;

	@NotNull(message = "Genre is required, Available genres : ACTION,COMEDY,FANTASY,MYSTERY,SCIENCE_FICTION,DRAMA")
	public Genre genre;

}
