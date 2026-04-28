package com.dare.cinema_booking_system.movies.exceptions;

public class MovieNotFoundException extends RuntimeException{

	public MovieNotFoundException(Long id) {
		super("Could not find movie with id: " + id);
	}
}
