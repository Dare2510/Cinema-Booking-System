package com.dare.cinema_booking_system.reservations.service;

import com.dare.cinema_booking_system.reservations.dto.PaymentResponse;
import com.dare.cinema_booking_system.reservations.dto.ReservationRequest;
import com.dare.cinema_booking_system.reservations.dto.ReservationResponse;
import com.dare.cinema_booking_system.reservations.entity.*;
import com.dare.cinema_booking_system.reservations.exceptions.ReservationCancelNotOnTimeException;
import com.dare.cinema_booking_system.reservations.exceptions.ReservationInvalidStatusFlowException;
import com.dare.cinema_booking_system.reservations.exceptions.ReservationNotFoundException;
import com.dare.cinema_booking_system.reservations.repository.ReservationsRepository;
import com.dare.cinema_booking_system.screenings.entity.ScreeningEntity;
import com.dare.cinema_booking_system.screenings.entity.ScreeningSeatEntity;
import com.dare.cinema_booking_system.screenings.entity.ScreeningSeatStatus;
import com.dare.cinema_booking_system.screenings.entity.TimeSlot;
import com.dare.cinema_booking_system.screenings.exceptions.ScreeningSeatNotAvailableException;
import com.dare.cinema_booking_system.screenings.repository.ScreeningSeatRepository;
import com.dare.cinema_booking_system.screenings.service.ScreeningSeatService;
import com.dare.cinema_booking_system.screenings.service.ScreeningService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ReservationService {

	private final ReservationsRepository reservationsRepository;
	private final ScreeningSeatService screeningSeatService;
	private final ScreeningService screeningService;
	private final PaymentService paymentService;
	private final TicketService ticketService;
	private final Clock clock;
	private final ScreeningSeatRepository screeningSeatRepository;

	public ReservationResponse findReservationById(Long reservationId) {
		ReservationEntity reservation = getReservationById(reservationId);
		return responseBuilder(reservation, reservation.getTicket(), reservation.getReservedSeats());
	}

	public Page<ReservationResponse> getPageOfReservations(Pageable pageable) {
		return reservationsRepository.findAll(pageable)
				.map(reservation -> {
					log.info("Getting Page of Reservations");
					return ReservationResponse.builder()
							.screeningDate(reservation.getScreening().getScreeningDate())
							.timeSlot(reservation.getScreening().getTimeSlot())
							.reservedSeats(reservation.getReservedSeats().stream()
									.map(seat -> "Row: " + seat.getCinemaSeats().getRowNumber()
											+ " Seat number: " + seat.getCinemaSeats().getSeatNumber()
											+ " status: " + seat.getScreeningSeatStatus())
									.toList())
							.build();


				});
	}

	@Transactional
	public ReservationResponse createReservation(ReservationRequest reservationRequest) {
		ScreeningEntity screeningToReserve = screeningService.getScreeningEntity(reservationRequest.getScreeningId());
		boolean seatsAreFree = screeningSeatService.seatsAreFree(screeningToReserve,reservationRequest);
		if (seatsAreFree) {
			ReservationEntity newReservation = reservationSaver();

			PaymentEntity payment = paymentService.createPayment(reservationRequest, screeningToReserve, newReservation);
			TicketEntity ticket = ticketService.createTicket(newReservation);
			List<ScreeningSeatEntity> reservedSeats = screeningSeatService.seatStatusUpdater(screeningToReserve, reservationRequest);
			reservationUpdater(newReservation, payment, ticket, screeningToReserve, reservedSeats);

			return responseBuilder(newReservation, ticket, reservedSeats);

		} else {
			log.warn("Chosen seats are not free , seats {} ", reservationRequest.getCinemaRoomSeatIds().stream().toList());
			throw new ScreeningSeatNotAvailableException();
		}


	}

	@Transactional
	public void cancelReservation(Long reservationId) {
		ReservationEntity toCancel = getReservationById(reservationId);
		PaymentEntity paymentToCancel = paymentService.findPaymentByReservationId(reservationId);
		TicketEntity ticketToCancel = ticketService.findTicketById(toCancel.getTicket().getId());
		List<ScreeningSeatEntity> seatsToCancel = toCancel.getReservedSeats();

		boolean onTime = cancelIsOnTime(toCancel);
		ReservationStatus currentStatus = toCancel.getReservationStatus();
		boolean newStatusIsValid = currentStatus.correctStatusOrder(toCancel, ReservationStatus.CANCELLED);


		if (onTime && newStatusIsValid) {
			toCancel.setReservationStatus(ReservationStatus.CANCELLED);
			ticketToCancel.setTicketStatus(TicketStatus.CANCELLED);
			seatsToCancel.forEach(seat -> {
				seat.setScreeningSeatStatus(ScreeningSeatStatus.FREE);
			});

			reservationsRepository.save(toCancel);
			ticketService.saveTicket(ticketToCancel);
			screeningSeatRepository.saveAll(seatsToCancel);

			paymentService.statusUpdateCancelPayment(currentStatus, paymentToCancel);
		}
		if (!onTime) {
			throw new ReservationCancelNotOnTimeException(reservationId);
		}
		if (!newStatusIsValid) {
			throw new ReservationInvalidStatusFlowException(reservationId, ReservationStatus.CANCELLED);
		}
	}

	//Helper Methods
	private ReservationEntity getReservationById(Long reservationId) {
		return reservationsRepository.findById(reservationId).orElseThrow(
				() -> {
					log.warn("Could not find reservation with id {}", reservationId);
					return new ReservationNotFoundException(reservationId);
				});
	}

	private void reservationUpdater(ReservationEntity newReservation, PaymentEntity payment, TicketEntity ticket,
	                                ScreeningEntity screeningToReserve, List<ScreeningSeatEntity> reservedSeats) {

		newReservation.setPayment(payment);
		newReservation.setTicket(ticket);
		newReservation.setScreening(screeningToReserve);
		newReservation.setReservedSeats(reservedSeats);
		reservationsRepository.save(newReservation);
		log.info("Reservation with id {} has been updated", newReservation.getId());

	}

	private ReservationEntity reservationSaver() {
		ReservationEntity reservationEntity = new ReservationEntity();
		reservationsRepository.save(reservationEntity);
		log.info("Reservation with id {} has been created", reservationEntity.getId());
		return reservationEntity;
	}

	private boolean cancelIsOnTime(ReservationEntity reservation) {
		ScreeningEntity screeningToCancel = screeningService.getScreeningEntity(reservation.getScreening().getId());
		TimeSlot timeSlot = screeningToCancel.getTimeSlot();
		LocalDate date = screeningToCancel.getScreeningDate();

		LocalDateTime currentDateTime = LocalDateTime.now(clock);

		return date.atTime(timeSlot.getStartTime())
				.isAfter(currentDateTime.plusMinutes(60));

	}



	private ReservationResponse responseBuilder(ReservationEntity newReservation, TicketEntity tickets, List<ScreeningSeatEntity> listOfReservedSeats) {
		List<String> reservedSeats = listOfReservedSeats.stream().
				map(spot -> "Row: " + spot.getCinemaSeats().getRowNumber() + " - "
						+ "Seat: " + spot.getCinemaSeats().getSeatNumber()).toList();

		if (newReservation.getPayment().getPaymentMethod() == PaymentMethod.ONLINE) {
			return ReservationResponse.builder()
					.reservationId(newReservation.getId())
					.reservedSeats(reservedSeats)
					.screeningDate(newReservation.getScreening().getScreeningDate())
					.timeSlot(newReservation.getScreening().getTimeSlot())
					.ticketNumber(tickets.getTicketNumber())
					.paymentResponse(PaymentResponse.builder()
							.paymentInformation("Payment information")
							.recipient("Cinema-Booking System")
							.bankName("Bank of Europe")
							.iban("DE75 5121 0800 1245 1261 99")
							.paymentReference(newReservation.getTicket().getTicketNumber())
							.amount(newReservation.getPayment().getAmount())
							.paymentStatus(newReservation.getPayment().getPaymentStatus()).build()
					)
					.build();
		} else {
			return ReservationResponse.builder()
					.reservationId(newReservation.getId())
					.reservedSeats(reservedSeats)
					.screeningDate(newReservation.getScreening().getScreeningDate())
					.timeSlot(newReservation.getScreening().getTimeSlot())
					.ticketNumber(tickets.getTicketNumber())
					.paymentResponse(PaymentResponse.builder()
							.amount(newReservation.getPayment().getAmount())
							.paymentStatus(newReservation.getPayment().getPaymentStatus()).build())
					.build();
		}
	}


}
