package com.dare.cinema_booking_system.movies.exceptions;

public class MovieByDurationNotFoundException extends RuntimeException {

	public MovieByDurationNotFoundException(int duration) {
		super("No movies with greater than " + duration + " min duration found");
	}
}
