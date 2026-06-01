package com.dare.cinema_booking_system.movie.exceptions;

import com.dare.cinema_booking_system.movie.entity.Genre;

public class MovieByGenreNotFoundException extends RuntimeException {

	public MovieByGenreNotFoundException(Genre genre) {
		super("No movies with " + genre + " as genre were found");
	}
}
