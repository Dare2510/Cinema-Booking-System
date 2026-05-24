package com.dare.cinema_booking_system.reservations.service;

import com.dare.cinema_booking_system.movies.service.MovieService;
import com.dare.cinema_booking_system.reservations.dto.ReservationsRequest;
import com.dare.cinema_booking_system.reservations.dto.ReservationsResponse;
import com.dare.cinema_booking_system.reservations.entity.PaymentEntity;
import com.dare.cinema_booking_system.reservations.entity.PaymentMethod;
import com.dare.cinema_booking_system.reservations.entity.ReservationEntity;
import com.dare.cinema_booking_system.reservations.entity.TicketEntity;
import com.dare.cinema_booking_system.reservations.exceptions.ReservationNotFoundException;
import com.dare.cinema_booking_system.reservations.repository.PaymentRepository;
import com.dare.cinema_booking_system.reservations.repository.ReservationsRepository;
import com.dare.cinema_booking_system.reservations.repository.TicketRepository;
import com.dare.cinema_booking_system.screenings.dto.ScreeningSeatResponse;
import com.dare.cinema_booking_system.screenings.entity.ScreeningSeatEntity;
import com.dare.cinema_booking_system.screenings.entity.ScreeningSeatStatus;
import com.dare.cinema_booking_system.screenings.entity.ScreeningsEntity;
import com.dare.cinema_booking_system.screenings.exceptions.ScreeningSeatNotAvailableException;
import com.dare.cinema_booking_system.screenings.repository.ScreeningSeatRepository;
import com.dare.cinema_booking_system.screenings.service.ScreeningsService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

	@Transactional
	public ReservationsResponse createReservation(ReservationsRequest reservationsRequest) {
		ScreeningsEntity screeningToReserve = screeningsService.getScreeningEntity(reservationsRequest.getScreeningId());
		boolean seatsAreFree = screeningSeatRepository.areAllSeatsFree(screeningToReserve.getId(), reservationsRequest.getScreeningSeatId());

		if (seatsAreFree) {
			ReservationEntity newReservation = new ReservationEntity();
			newReservation.setScreening(screeningToReserve);
			reservationsRepository.save(newReservation);

			int numberOfSeats = reservationsRequest.screeningSeatId.size();
			BigDecimal pricePerSeat = screeningToReserve.getPrice();
			BigDecimal finalAmount = pricePerSeat.multiply(BigDecimal.valueOf((double) numberOfSeats));
			PaymentMethod paymentMethod = reservationsRequest.getPaymentMethod();

			PaymentEntity reservationPayment = new PaymentEntity(newReservation, finalAmount, paymentMethod);
			List<PaymentEntity> payments = new ArrayList<>();
			payments.add(reservationPayment);
			paymentRepository.saveAll(payments);

			String ticketNumber = UUID.randomUUID().toString();
			List<ScreeningSeatEntity> listOfReservedSeats = screeningSeatRepository.findAllById(reservationsRequest.getScreeningSeatId());

			List<String> reservedSeats = listOfReservedSeats.stream().
					map(spot -> "Row: " + spot.getCinemaSeats().getRowNumber() + " - "
							+ "Seat: " + spot.getCinemaSeats().getSeatNumber()).toList();

			TicketEntity ticket = new TicketEntity(ticketNumber, newReservation);

			listOfReservedSeats.forEach(spot -> spot.setScreeningSeatStatus(ScreeningSeatStatus.RESERVED));
			screeningSeatRepository.saveAll(listOfReservedSeats);


			List<TicketEntity> tickets = new ArrayList<>();
			tickets.add(ticket);
			ticketRepository.saveAll(tickets);

			newReservation.setPayments(payments);
			newReservation.setTickets(tickets);
			newReservation.setScreening(screeningToReserve);
			reservationsRepository.save(newReservation);

			return ReservationsResponse.builder()
					.reservationId(newReservation.getId())
					.reservedSeats(reservedSeats)
					.screeningDate(newReservation.getScreening().getScreeningDate())
					.ticketNumber(ticketNumber)
					.build();
		} else {
			throw new ScreeningSeatNotAvailableException();
		}


	}


	//Helper Methods
	private ReservationEntity getReservationById(Long reservationId) {
		return reservationsRepository.findById(reservationId).orElseThrow(
				() -> new ReservationNotFoundException(reservationId)
		);
	}

	public List<ScreeningSeatResponse> getFreeScreeningSeatsByScreeningId(Long screeningId) {
		List<ScreeningSeatEntity> freeSeats = screeningSeatRepository.getFreeScreeningSeats(screeningId);

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
