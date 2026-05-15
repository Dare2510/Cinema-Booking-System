package com.dare.cinema_booking_system.movies.exceptions;

public class MovieUpdateNotPossibleException extends RuntimeException {
	public MovieUpdateNotPossibleException(Long movieId) {

		super("Movie with id " + movieId + " cannot be updated");
	}
}
