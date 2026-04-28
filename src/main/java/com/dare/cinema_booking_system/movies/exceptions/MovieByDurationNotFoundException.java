package com.dare.cinema_booking_system.movies.exceptions;

public class MovieByDurationNotFoundException extends RuntimeException {

	public MovieByDurationNotFoundException(int duration) {
		super("No movies with " + duration + " min duration found");
	}
}
