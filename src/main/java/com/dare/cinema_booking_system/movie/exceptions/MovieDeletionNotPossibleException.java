package com.dare.cinema_booking_system.movie.exceptions;

public class MovieDeletionNotPossibleException extends RuntimeException {
	public MovieDeletionNotPossibleException(Long movieId) {

		super("Movie with id " + movieId + " cannot be deleted, screening exits");
	}
}
