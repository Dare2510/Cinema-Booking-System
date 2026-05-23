package com.dare.cinema_booking_system.reservations.service;

import com.dare.cinema_booking_system.movies.service.MovieService;
import com.dare.cinema_booking_system.reservations.dto.ReservationsResponse;
import com.dare.cinema_booking_system.reservations.entity.ReservationEntity;
import com.dare.cinema_booking_system.reservations.exceptions.ReservationNotFoundException;
import com.dare.cinema_booking_system.reservations.repository.PaymentRepository;
import com.dare.cinema_booking_system.reservations.repository.ReservationsRepository;
import com.dare.cinema_booking_system.reservations.repository.TicketRepository;
import com.dare.cinema_booking_system.screenings.dto.ScreeningSeatResponse;
import com.dare.cinema_booking_system.screenings.entity.ScreeningSeatEntity;
import com.dare.cinema_booking_system.screenings.repository.ScreeningSeatRepository;
import com.dare.cinema_booking_system.screenings.service.ScreeningsService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ReservationsService {

	private final ReservationsRepository reservationsRepository;
	private final ScreeningSeatRepository screeningSeatRepository;
	private final PaymentRepository paymentRepository;
	private final TicketRepository ticketRepository;
	private final ScreeningsService screeningsService;
	private final MovieService movieService;
	private final ModelMapper modelMapper;

	public ReservationsResponse findReservationById(Long reservationId) {
		ReservationEntity reservation = getReservationById(reservationId);
		return modelMapper.map(reservation, ReservationsResponse.class);
	}






	//Helper Methods
	private ReservationEntity getReservationById(Long reservationId) {
		return reservationsRepository.findById(reservationId).orElseThrow(
				() -> new ReservationNotFoundException(reservationId)
		);
	}

	public List<ScreeningSeatResponse> getFreeScreeningSeatsByScreeningId(Long screeningId) {
		List<ScreeningSeatEntity>  freeSeats = screeningSeatRepository.getFreeScreeningSeats(screeningId);

		return freeSeats.stream()
				.map(seat -> ScreeningSeatResponse.builder()
						.screeningSeatsId(seat.getId())
						.seatNumber(seat.getCinemaSeats().getSeatNumber())
						.rowNumber(seat.getCinemaSeats().getRowNumber())
						.build()
				)
				.toList();
	}

}
