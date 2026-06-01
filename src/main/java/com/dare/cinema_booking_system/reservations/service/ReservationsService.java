package com.dare.cinema_booking_system.reservations.service;

import com.dare.cinema_booking_system.reservations.dto.PaymentResponse;
import com.dare.cinema_booking_system.reservations.dto.ReservationRequest;
import com.dare.cinema_booking_system.reservations.dto.ReservationResponse;
import com.dare.cinema_booking_system.reservations.entity.*;
import com.dare.cinema_booking_system.reservations.exceptions.*;
import com.dare.cinema_booking_system.reservations.repository.PaymentRepository;
import com.dare.cinema_booking_system.reservations.repository.ReservationsRepository;
import com.dare.cinema_booking_system.reservations.repository.TicketRepository;
import com.dare.cinema_booking_system.screenings.entity.ScreeningEntity;
import com.dare.cinema_booking_system.screenings.entity.ScreeningSeatEntity;
import com.dare.cinema_booking_system.screenings.entity.ScreeningSeatStatus;
import com.dare.cinema_booking_system.screenings.entity.TimeSlot;
import com.dare.cinema_booking_system.screenings.exceptions.ScreeningSeatNotAvailableException;
import com.dare.cinema_booking_system.screenings.repository.ScreeningSeatRepository;
import com.dare.cinema_booking_system.screenings.service.ScreeningService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class ReservationsService {

	private final ReservationsRepository reservationsRepository;
	private final ScreeningSeatRepository screeningSeatRepository;
	private final PaymentRepository paymentRepository;
	private final TicketRepository ticketRepository;
	private final ScreeningService screeningService;
	private final ModelMapper modelMapper;

	public ReservationResponse findReservationById(Long reservationId) {
		ReservationEntity reservation = getReservationById(reservationId);
		return modelMapper.map(reservation, ReservationResponse.class);
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
		boolean seatsAreFree = screeningSeatRepository.areAllCinemaRoomSeatsFree(screeningToReserve.getId(), reservationRequest.getCinemaRoomSeatIds(), reservationRequest.getCinemaRoomSeatIds().size());

		if (seatsAreFree) {
			ReservationEntity newReservation = reservationSaver();

			PaymentEntity payment = paymentCreator(reservationRequest, screeningToReserve, newReservation);
			TicketEntity ticket = ticketCreator(newReservation);
			List<ScreeningSeatEntity> reservedSeats = seatStatusUpdater(screeningToReserve, reservationRequest);
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
		PaymentEntity paymentToCancel = paymentRepository.findByReservation_Id(reservationId);
		TicketEntity ticketToCancel = getTicketById(toCancel.getTicket().getId());
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
			ticketRepository.save(ticketToCancel);
			screeningSeatRepository.saveAll(seatsToCancel);
			switch (currentStatus) {
				case CREATED:
					paymentToCancel.setPaymentStatus(PaymentStatus.REFUNDED);
					paymentRepository.save(paymentToCancel);
					break;
				case CONFIRMED:
					paymentToCancel.setPaymentStatus(PaymentStatus.REFUND_PENDING);
					paymentRepository.save(paymentToCancel);

			}
		}
		if (!onTime) {
			throw new ReservationCancelNotOnTimeException(reservationId);
		}
		if (!newStatusIsValid) {
			throw new ReservationInvalidStatusFlowException(reservationId, ReservationStatus.CANCELLED);
		}
	}

	@Transactional
	public void completeRefund(Long reservationId) {
		ReservationEntity toRefund = getReservationById(reservationId);
		PaymentStatus currentPaymentStatus = toRefund.getPayment().getPaymentStatus();
		ReservationStatus currentReservationStatus = toRefund.getReservationStatus();
		boolean validStatus = currentPaymentStatus.validatorToRefundPayment(currentReservationStatus, currentPaymentStatus);

		if (validStatus) {
			toRefund.getPayment().setPaymentStatus(PaymentStatus.REFUNDED);
			reservationsRepository.save(toRefund);
		} else {
			log.warn("Reservation with id {} has wrong status, payment status {} ,reservation status {}",
					reservationId, currentPaymentStatus, currentReservationStatus);
			throw new ReservationRefundException(reservationId);
		}

	}

	@Transactional
	public void completePayment(Long reservationId) {
		ReservationEntity toPay = getReservationById(reservationId);
		PaymentEntity payment = paymentRepository.findByReservation_Id(reservationId);
		PaymentStatus currentPaymentStatus = payment.getPaymentStatus();
		ReservationStatus currentReservationStatus = toPay.getReservationStatus();
		boolean validStatus = currentPaymentStatus.validatorToCompletePayment(currentReservationStatus, currentPaymentStatus);

		if (validStatus) {
			toPay.getPayment().setPaymentStatus(PaymentStatus.PAID);
			toPay.setReservationStatus(ReservationStatus.CONFIRMED);
			reservationsRepository.save(toPay);
			paymentRepository.save(payment);
		} else {
			log.warn("Reservation with id {} has wrong status, payment status {} ,reservation status {}",
					reservationId, currentPaymentStatus, currentReservationStatus);
			throw new ReservationCompletePaymentException(reservationId);
		}
	}

	public void setTicketToUsed(String ticketNumber) {
		TicketEntity ticket = getTicketByNumber(ticketNumber);
		TicketStatus ticketStatus = ticket.getTicketStatus();
		PaymentStatus paymentStatus = ticket.getReservation().getPayment().getPaymentStatus();
		boolean validStatus = ticketStatus.validatorForUsed(ticketStatus, paymentStatus);

		if (validStatus) {
			ticket.setTicketStatus(TicketStatus.USED);
			ticketRepository.save(ticket);
		} else {
			log.warn("{} has wrong status, ticket status: {}, payment status: {} ", ticketNumber, ticketStatus, paymentStatus);
			throw new TicketUseNotPossibleException(ticketNumber);
		}


	}

	public void setStatusOfExpiredTickets() {
		LocalDate dateNow = LocalDate.now();
		LocalTime timeNow = LocalTime.now();

		List<TicketEntity> expiredTickets = ticketRepository.getExpiredTickets(dateNow, timeNow);
		if (!expiredTickets.isEmpty()) {
			expiredTickets.forEach(ticket -> {
				log.info("Ticket with ID {} has been expired", ticket.getId());
				ticket.setTicketStatus(TicketStatus.EXPIRED);
			});
			ticketRepository.saveAll(expiredTickets);
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

	private TicketEntity getTicketById(Long ticketId) {
		return ticketRepository.findById(ticketId).orElseThrow(
				() -> {
					log.warn("Could not find ticket with id {}", ticketId);
					return new TicketNotFoundException(ticketId);
				});
	}

	private TicketEntity getTicketByNumber(String ticketNumber) {
		return ticketRepository.findByTicketNumber(ticketNumber).orElseThrow(
				() -> {
					log.warn("Could not find ticket with number {}", ticketNumber);
					return new TicketNotFoundException(ticketNumber);
				}
		);
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

	private PaymentEntity paymentCreator(ReservationRequest reservationRequest, ScreeningEntity screeningToReserve, ReservationEntity newReservation) {
		int numberOfSeats = reservationRequest.getCinemaRoomSeatIds().size();
		BigDecimal pricePerSeat = screeningToReserve.getPrice();
		BigDecimal finalAmount = pricePerSeat.multiply(BigDecimal.valueOf((double) numberOfSeats));
		PaymentMethod paymentMethod = reservationRequest.getPaymentMethod();

		PaymentEntity payments = new PaymentEntity(newReservation, finalAmount, paymentMethod);

		paymentRepository.save(payments);
		log.info("Payment with id {} has been created", payments.getId());

		return payments;

	}

	private TicketEntity ticketCreator(ReservationEntity newReservation) {
		String ticketNumber = UUID.randomUUID().toString();

		TicketEntity ticket = new TicketEntity(ticketNumber, newReservation);
		ticketRepository.save(ticket);
		log.info("Ticket with id {} has been created", ticket.getId());

		return ticket;
	}

	private boolean cancelIsOnTime(ReservationEntity reservation) {
		ScreeningEntity screeningToCancel = screeningService.getScreeningEntity(reservation.getScreening().getId());
		TimeSlot timeSlot = screeningToCancel.getTimeSlot();
		LocalDate date = screeningToCancel.getScreeningDate();

		LocalDateTime currentDateTime = LocalDateTime.now();

		int time = (timeSlot == TimeSlot.EVENING) ? 17 : (timeSlot == TimeSlot.PRIME) ? 20 : 23;
		return date.atTime(time, 0).isAfter(currentDateTime.plusMinutes(60));

	}

	private List<ScreeningSeatEntity> seatStatusUpdater(ScreeningEntity screeningToReserve, ReservationRequest reservationRequest) {
		List<ScreeningSeatEntity> allSeats = screeningSeatRepository.getScreeningSeatsByScreening(screeningToReserve);
		List<Long> targetSeatIds = reservationRequest.getCinemaRoomSeatIds();

		List<ScreeningSeatEntity> seatsToReserve = allSeats.stream()
				.filter(seat -> targetSeatIds.contains(seat.getCinemaSeats().getId()))
				.collect(Collectors.toCollection(ArrayList::new));

		seatsToReserve.forEach(seat -> {
			seat.setScreeningSeatStatus(ScreeningSeatStatus.RESERVED);
		});

		screeningSeatRepository.saveAll(seatsToReserve);

		log.info("Seats with id's {} have been updated", targetSeatIds);

		return seatsToReserve;

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
					.ticketNumber(tickets.getTicketNumber())
					.paymentResponse(PaymentResponse.builder()
							.amount(newReservation.getPayment().getAmount())
							.paymentStatus(newReservation.getPayment().getPaymentStatus()).build())
					.build();
		}
	}


}
