package com.dare.cinema_booking_system.common;

import com.dare.cinema_booking_system.movie.exceptions.*;
import com.dare.cinema_booking_system.reservations.exceptions.*;
import com.dare.cinema_booking_system.rooms.exceptions.CinemaRoomChangesNotPossibleException;
import com.dare.cinema_booking_system.rooms.exceptions.CinemaRoomNotFoundException;
import com.dare.cinema_booking_system.rooms.exceptions.CinemaRoomNumberDuplicateException;
import com.dare.cinema_booking_system.screenings.exceptions.ScreeningNotFoundException;
import com.dare.cinema_booking_system.screenings.exceptions.ScreeningSeatNotAvailableException;
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

	@ExceptionHandler(MovieDeletionNotPossibleException.class)
	public ResponseEntity<ErrorResponse> handleMovieDeletionNotPossibleException(MovieDeletionNotPossibleException ex,
	                                                                             HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				ex.getMessage(),
				request.getRequestURI(),
				LocalDateTime.now()
		);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
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

	@ExceptionHandler(ScreeningSeatNotAvailableException.class)
	public ResponseEntity<ErrorResponse> handleScreeningSeatNotAvailableException(ScreeningSeatNotAvailableException ex,
	                                                                              HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				ex.getMessage(),
				request.getRequestURI(),
				LocalDateTime.now()
		);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ReservationNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleReservationNotFoundException(ReservationNotFoundException ex,
	                                                                        HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.NOT_FOUND.value(),
				ex.getMessage(),
				request.getRequestURI(),
				LocalDateTime.now()
		);
		return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(ReservationCancelNotOnTimeException.class)
	public ResponseEntity<ErrorResponse> handleReservationCancelNotOnTimeException(ReservationCancelNotOnTimeException ex,
	                                                                               HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				ex.getMessage(),
				request.getRequestURI(),
				LocalDateTime.now()
		);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ReservationInvalidStatusFlowException.class)
	public ResponseEntity<ErrorResponse> handleReservationInvalidStatusFlowException(ReservationInvalidStatusFlowException ex,
	                                                                                 HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				ex.getMessage(),
				request.getRequestURI(),
				LocalDateTime.now()
		);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ReservationRefundException.class)
	public ResponseEntity<ErrorResponse> handleReservationRefundException(ReservationRefundException ex,
	                                                                      HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				ex.getMessage(),
				request.getRequestURI(),
				LocalDateTime.now()
		);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ReservationCompletePaymentException.class)
	public ResponseEntity<ErrorResponse> handleReservationCompletePaymentException(ReservationCompletePaymentException ex,
	                                                                               HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				ex.getMessage(),
				request.getRequestURI(),
				LocalDateTime.now()
		);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(TicketNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleTicketNotFoundException(TicketNotFoundException ex,
	                                                                   HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.NOT_FOUND.value(),
				ex.getMessage(),
				request.getRequestURI(),
				LocalDateTime.now()
		);
		return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(TicketUseNotPossibleException.class)
	public ResponseEntity<ErrorResponse> handleTicketUseNotPossibleException(TicketUseNotPossibleException ex,
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
