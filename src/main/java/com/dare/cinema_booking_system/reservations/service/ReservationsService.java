package com.dare.cinema_booking_system.reservations.service;

import com.dare.cinema_booking_system.movies.service.MovieService;
import com.dare.cinema_booking_system.reservations.dto.ReservationsRequest;
import com.dare.cinema_booking_system.reservations.dto.ReservationsResponse;
import com.dare.cinema_booking_system.reservations.entity.*;
import com.dare.cinema_booking_system.reservations.exceptions.ReservationNotFoundException;
import com.dare.cinema_booking_system.reservations.repository.PaymentRepository;
import com.dare.cinema_booking_system.reservations.repository.ReservationsRepository;
import com.dare.cinema_booking_system.reservations.repository.TicketRepository;
import com.dare.cinema_booking_system.screenings.dto.ScreeningSeatResponse;
import com.dare.cinema_booking_system.screenings.entity.ScreeningSeatEntity;
import com.dare.cinema_booking_system.screenings.entity.ScreeningSeatStatus;
import com.dare.cinema_booking_system.screenings.entity.ScreeningsEntity;
import com.dare.cinema_booking_system.screenings.entity.TimeSlot;
import com.dare.cinema_booking_system.screenings.exceptions.ScreeningSeatNotAvailableException;
import com.dare.cinema_booking_system.screenings.repository.ScreeningSeatRepository;
import com.dare.cinema_booking_system.screenings.repository.ScreeningsRepository;
import com.dare.cinema_booking_system.screenings.service.ScreeningsService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class ReservationsService {

	private final ReservationsRepository reservationsRepository;
	private final ScreeningSeatRepository screeningSeatRepository;
	private final PaymentRepository paymentRepository;
	private final TicketRepository ticketRepository;
	private final ScreeningsService screeningsService;
	private final MovieService movieService;
	private final ModelMapper modelMapper;
	private final ScreeningsRepository screeningsRepository;

	public ReservationsResponse findReservationById(Long reservationId) {
		ReservationEntity reservation = getReservationById(reservationId);
		return modelMapper.map(reservation, ReservationsResponse.class);
	}

	@Transactional
	public ReservationsResponse createReservation(ReservationsRequest reservationsRequest) {
		ScreeningsEntity screeningToReserve = screeningsService.getScreeningEntity(reservationsRequest.getScreeningId());
		boolean seatsAreFree = screeningSeatRepository.areAllSeatsFree(screeningToReserve.getId(), reservationsRequest.getScreeningSeatId());

		if (seatsAreFree) {
			ReservationEntity newReservation = reservationSaver();

			List<PaymentEntity> payments = paymentCreator(reservationsRequest,screeningToReserve,newReservation);
			List<TicketEntity> tickets = ticketCreator(newReservation);
			List<ScreeningSeatEntity> reservedSeats = seatStatusUpdater(reservationsRequest);
			reservationUpdater(newReservation,payments,tickets,screeningToReserve);

			return responseBuilder(newReservation,tickets,reservedSeats);

		} else {
			throw new ScreeningSeatNotAvailableException();
		}


	}

	public void cancelReservation(Long reservationId) {
		ReservationEntity toCancel = getReservationById(reservationId);
		PaymentEntity paymentToCancel = paymentRepository.findByReservation_Id(reservationId);
		TicketEntity ticketToCancel = ticketRepository.findByReservation_Id(reservationId);
		List<ScreeningSeatEntity> seatsToCancel = toCancel.getScreening().getScreeningSeats();

		boolean onTime = cancelIsOnTime(toCancel);
		ReservationStatus currentStatus = toCancel.getReservationStatus();
		boolean newStatusIsValid = currentStatus.correctStatusOrder(toCancel, ReservationStatus.CANCELLED);


			if(onTime &&  newStatusIsValid) {
				toCancel.setReservationStatus(ReservationStatus.CANCELLED);
				ticketToCancel.setTicketStatus(TicketStatus.CANCELLED);
				seatsToCancel.forEach(seat -> {seat.setScreeningSeatStatus(ScreeningSeatStatus.FREE);});

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
			} else {
				throw new RuntimeException("Cancel not possible");
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

	private void reservationUpdater(ReservationEntity newReservation, List<PaymentEntity> payments, List<TicketEntity> tickets,ScreeningsEntity screeningToReserve) {

		newReservation.setPayments(payments);
		newReservation.setTickets(tickets);
		newReservation.setScreening(screeningToReserve);
		reservationsRepository.save(newReservation);
		log.info("Reservation with id {} has been updated", newReservation.getId());

	}

	private ReservationEntity reservationSaver() {
		ReservationEntity reservationEntity = new ReservationEntity();
		reservationsRepository.save(reservationEntity);
		log.info("Reservation with id {} has been created", reservationEntity.getId());
		return reservationEntity;
	}

	private List<PaymentEntity> paymentCreator(ReservationsRequest reservationsRequest, ScreeningsEntity screeningToReserve, ReservationEntity newReservation) {
		int numberOfSeats = reservationsRequest.screeningSeatId.size();
		BigDecimal pricePerSeat = screeningToReserve.getPrice();
		BigDecimal finalAmount = pricePerSeat.multiply(BigDecimal.valueOf((double) numberOfSeats));
		PaymentMethod paymentMethod = reservationsRequest.getPaymentMethod();

		List<PaymentEntity> payments = new ArrayList<>();

		payments.add(new PaymentEntity(newReservation, finalAmount, paymentMethod));
		paymentRepository.saveAll(payments);
		log.info("Payment with id {} has been created", payments.get(0).getId());

		return payments;

	}

	private List<TicketEntity> ticketCreator(ReservationEntity newReservation) {
		String ticketNumber = UUID.randomUUID().toString();

		List<TicketEntity> tickets = new ArrayList<>();
		tickets.add(new TicketEntity(ticketNumber, newReservation));
		ticketRepository.saveAll(tickets);
		log.info("Ticket with id {} has been created", tickets.get(0).getId());

		return tickets;
	}

	private List<ScreeningSeatEntity> seatStatusUpdater(ReservationsRequest reservationsRequest) {
		List<ScreeningSeatEntity> listOfReservedSeats = screeningSeatRepository.findAllById(reservationsRequest.getScreeningSeatId());

		listOfReservedSeats.forEach(spot -> spot.setScreeningSeatStatus(ScreeningSeatStatus.RESERVED));
		screeningSeatRepository.saveAll(listOfReservedSeats);
		log.info("Seats with id's {} has been updated" , listOfReservedSeats.stream().map(ScreeningSeatEntity::getId).toList());

		return listOfReservedSeats;

	}

	private ReservationsResponse responseBuilder(ReservationEntity newReservation,List<TicketEntity> tickets, List<ScreeningSeatEntity> listOfReservedSeats) {
		List<String> reservedSeats = listOfReservedSeats.stream().
				map(spot -> "Row: " + spot.getCinemaSeats().getRowNumber() + " - "
						+ "Seat: " + spot.getCinemaSeats().getSeatNumber()).toList();

		return ReservationsResponse.builder()
				.reservationId(newReservation.getId())
				.reservedSeats(reservedSeats)
				.screeningDate(newReservation.getScreening().getScreeningDate())
				.ticketNumber(tickets.get(0).getTicketNumber())
				.build();
	}

	private boolean cancelIsOnTime(ReservationEntity reservation) {
		ScreeningsEntity screeningToCancel = screeningsService.getScreeningEntity(reservation.getId());
		TimeSlot timeSlot = screeningToCancel.getTimeSlot();
		LocalDate date = screeningToCancel.getScreeningDate();

		LocalDateTime currentDateTime = LocalDateTime.now();

		int time = (timeSlot==TimeSlot.EVENING_18H)? 18 : (timeSlot==TimeSlot.PRIME_20H) ? 20 : 22;
		return date.atTime(time, 0).isAfter(currentDateTime.plusMinutes(60));

	}

}
