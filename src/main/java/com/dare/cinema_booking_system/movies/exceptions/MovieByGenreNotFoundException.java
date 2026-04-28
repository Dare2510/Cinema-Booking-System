package com.dare.cinema_booking_system.movies.exceptions;

import com.dare.cinema_booking_system.movies.entity.Genre;

public class MovieByGenreNotFoundException extends RuntimeException {

	public MovieByGenreNotFoundException(Genre genre) {
		super("No movies with " + genre + " found");
	}
}
