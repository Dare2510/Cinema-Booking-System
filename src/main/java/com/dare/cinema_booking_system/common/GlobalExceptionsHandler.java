package com.dare.cinema_booking_system.common;

import com.dare.cinema_booking_system.movies.exceptions.MovieByDurationNotFoundException;
import com.dare.cinema_booking_system.movies.exceptions.MovieByGenreNotFoundException;
import com.dare.cinema_booking_system.movies.exceptions.MovieNotFoundException;
import com.dare.cinema_booking_system.movies.exceptions.MovieUpdateNotPossibleException;
import com.dare.cinema_booking_system.rooms.exceptions.CinemaRoomChangesNotPossibleException;
import com.dare.cinema_booking_system.rooms.exceptions.CinemaRoomNotFoundException;
import com.dare.cinema_booking_system.rooms.exceptions.CinemaRoomNumberDuplicateException;
import com.dare.cinema_booking_system.screenings.exceptions.ScreeningNotFoundException;
import com.dare.cinema_booking_system.screenings.exceptions.ScreeningSlotAlreadyBookedException;
import com.dare.cinema_booking_system.screenings.exceptions.ScreeningUpdateNotPossibleException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionsHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex,
																			   HttpServletRequest request) {
			ErrorResponse error = new ErrorResponse(
					HttpStatus.BAD_REQUEST.value(),
					ex.getAllErrors().get(0).getDefaultMessage(),
					request.getRequestURI(),
					LocalDateTime.now()
					);
			return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MovieByDurationNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleMovieByDurationNotFoundException(MovieByDurationNotFoundException ex,
																				HttpServletRequest request) {

			ErrorResponse error = new ErrorResponse(
					HttpStatus.NOT_FOUND.value(),
					ex.getMessage(),
					request.getRequestURI(),
					LocalDateTime.now()
			);
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(MovieByGenreNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleMovieByGenreNotFoundException(MovieByGenreNotFoundException ex,
																			 HttpServletRequest request) {
			ErrorResponse error = new ErrorResponse(
					HttpStatus.NOT_FOUND.value(),
					ex.getMessage(),
					request.getRequestURI(),
					LocalDateTime.now()
			);
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(MovieNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleMovieNotFoundException(MovieNotFoundException ex,
																		 HttpServletRequest request) {
			ErrorResponse error = new ErrorResponse(
					HttpStatus.NOT_FOUND.value(),
					ex.getMessage(),
					request.getRequestURI(),
					LocalDateTime.now()
			);
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);

	}

	@ExceptionHandler(CinemaRoomNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleCinemaRoomNotFoundException(CinemaRoomNotFoundException ex,
																		   HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.NOT_FOUND.value(),
				ex.getMessage(),
				request.getRequestURI(),
				LocalDateTime.now()
		);
		return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(CinemaRoomNumberDuplicateException.class)
	public ResponseEntity<ErrorResponse> handleCinemaRoomNumberDuplicateException(CinemaRoomNumberDuplicateException ex,
																				  HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				ex.getMessage(),
				request.getRequestURI(),
				LocalDateTime.now()
		);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ScreeningNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleScreeningNotFoundException(ScreeningNotFoundException ex,
																				HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.NOT_FOUND.value(),
				ex.getMessage(),
				request.getRequestURI(),
				LocalDateTime.now()
		);
		return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(ScreeningSlotAlreadyBookedException.class)
	public ResponseEntity<ErrorResponse> handleScreeningSlotAlreadyBookedException(ScreeningSlotAlreadyBookedException ex,
																				   HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				ex.getMessage(),
				request.getRequestURI(),
				LocalDateTime.now()
		);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ScreeningUpdateNotPossibleException.class)
	public ResponseEntity<ErrorResponse> handleScreeningUpdateNotPossibleException(ScreeningUpdateNotPossibleException ex,
																				   HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				ex.getMessage(),
				request.getRequestURI(),
				LocalDateTime.now()
	);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(CinemaRoomChangesNotPossibleException.class)
	public ResponseEntity<ErrorResponse> handleCinemaRoomChangesNotPossibleException(CinemaRoomChangesNotPossibleException ex,
																					 HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				ex.getMessage(),
				request.getRequestURI(),
				LocalDateTime.now()
		);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MovieUpdateNotPossibleException.class)
	public ResponseEntity<ErrorResponse> handleMovieUpdateNotPossibleException(MovieUpdateNotPossibleException ex,
																		   HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				ex.getMessage(),
				request.getRequestURI(),
				LocalDateTime.now()
		);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}
}
