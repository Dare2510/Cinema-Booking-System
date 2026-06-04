package com.dare.cinema_booking_system.service;

import com.dare.cinema_booking_system.movie.entity.Genre;
import com.dare.cinema_booking_system.movie.entity.MovieEntity;
import com.dare.cinema_booking_system.reservations.dto.ReservationRequest;
import com.dare.cinema_booking_system.reservations.dto.ReservationResponse;
import com.dare.cinema_booking_system.reservations.entity.*;
import com.dare.cinema_booking_system.reservations.exceptions.ReservationCancelNotOnTimeException;
import com.dare.cinema_booking_system.reservations.exceptions.ReservationCompletePaymentException;
import com.dare.cinema_booking_system.reservations.exceptions.ReservationInvalidStatusFlowException;
import com.dare.cinema_booking_system.reservations.exceptions.TicketUseNotPossibleException;
import com.dare.cinema_booking_system.reservations.repository.PaymentRepository;
import com.dare.cinema_booking_system.reservations.repository.ReservationsRepository;
import com.dare.cinema_booking_system.reservations.repository.TicketRepository;
import com.dare.cinema_booking_system.reservations.service.ReservationsService;
import com.dare.cinema_booking_system.rooms.entity.SeatEntity;
import com.dare.cinema_booking_system.screenings.entity.ScreeningEntity;
import com.dare.cinema_booking_system.screenings.entity.ScreeningSeatEntity;
import com.dare.cinema_booking_system.screenings.entity.ScreeningSeatStatus;
import com.dare.cinema_booking_system.screenings.entity.TimeSlot;
import com.dare.cinema_booking_system.screenings.exceptions.ScreeningSeatNotAvailableException;
import com.dare.cinema_booking_system.screenings.repository.ScreeningSeatRepository;
import com.dare.cinema_booking_system.screenings.service.ScreeningService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

	private static final Long RESERVATION_ID = 1L;
	private static final Long SCREENING_ID = 1L;
	private static final Long TICKET_ID = 1L;
	private static final Long PAYMENT_ID = 1L;

	private static final List<Long> SEAT_IDS = List.of(1L, 2L);
	private static final long SEAT_COUNT = 2L;
	private static final BigDecimal PRICE_PER_SEAT = BigDecimal.valueOf(10);

	@InjectMocks
	private ReservationsService reservationsService;

	@Mock
	private ReservationsRepository reservationsRepository;

	@Mock
	private ScreeningSeatRepository screeningSeatRepository;

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private TicketRepository ticketRepository;

	@Mock
	private ScreeningService screeningService;

	@Mock
	private ModelMapper modelMapper;

	@Test
	void createReservation_whenReservedSeatsAreFree_returnsReservationResponse() {
		ScreeningEntity screening = screening(LocalDate.now(), TimeSlot.PRIME);
		List<ScreeningSeatEntity> seats = screeningSeats(ScreeningSeatStatus.FREE);
		ReservationRequest request = onlineReservationRequest();

		mockScreening(screening);
		mockSeatsAreFree(request, true);
		mockScreeningSeats(screening, seats);
		mockCreateReservationSaves();

		ReservationResponse response = reservationsService.createReservation(request);

		assertNotNull(response);
		assertEquals(RESERVATION_ID, response.getReservationId());
		assertEquals(screening.getScreeningDate(), response.getScreeningDate());
		assertNotNull(response.getTicketNumber());

		assertEquals(2, response.getReservedSeats().size());
		assertTrue(response.getReservedSeats().contains("Row: 1 - Seat: 1"));
		assertTrue(response.getReservedSeats().contains("Row: 1 - Seat: 2"));

		assertNotNull(response.getPaymentResponse());
		assertEquals(0, BigDecimal.valueOf(20).compareTo(response.getPaymentResponse().getAmount()));

		assertEquals(ScreeningSeatStatus.RESERVED, seats.get(0).getScreeningSeatStatus());
		assertEquals(ScreeningSeatStatus.RESERVED, seats.get(1).getScreeningSeatStatus());

		verify(screeningService).getScreeningEntity(SCREENING_ID);
		verify(screeningSeatRepository).areAllCinemaRoomSeatsFree(
				eq(SCREENING_ID),
				eq(SEAT_IDS),
				eq(SEAT_COUNT)
		);
		verify(reservationsRepository, times(2)).save(any(ReservationEntity.class));
		verify(paymentRepository).save(any(PaymentEntity.class));
		verify(ticketRepository).save(any(TicketEntity.class));
		verify(screeningSeatRepository).saveAll(seats);
	}

	@Test
	void createReservation_whenSeatsAreAlreadyReserved_throwsScreeningSeatNotAvailableException() {
		ScreeningEntity screening = screening(LocalDate.now(), TimeSlot.PRIME);
		ReservationRequest request = onlineReservationRequest();

		mockScreening(screening);
		mockSeatsAreFree(request, false);

		assertThatThrownBy(() -> reservationsService.createReservation(request))
				.isInstanceOf(ScreeningSeatNotAvailableException.class)
				.hasMessage("Chosen seats are not available");

		verify(screeningService).getScreeningEntity(SCREENING_ID);
		verify(screeningSeatRepository).areAllCinemaRoomSeatsFree(
				eq(SCREENING_ID),
				eq(SEAT_IDS),
				eq(SEAT_COUNT)
		);

		verifyNoReservationCreation();
	}

	@Test
	void cancelReservation_whenCancelIsOnTimeAndStatusValid_cancelsReservation() {
		ScreeningEntity screening = screening(LocalDate.now().plusDays(1), TimeSlot.PRIME);
		List<ScreeningSeatEntity> seats = screeningSeats(ScreeningSeatStatus.RESERVED);
		ReservationEntity reservation = reservation(
				ReservationStatus.CREATED,
				PaymentStatus.UNPAID,
				TicketStatus.VALID,
				screening,
				seats
		);

		mockExistingReservation(reservation);
		when(screeningService.getScreeningEntity(SCREENING_ID)).thenReturn(screening);

		reservationsService.cancelReservation(RESERVATION_ID);

		assertEquals(ReservationStatus.CANCELLED, reservation.getReservationStatus());
		assertEquals(TicketStatus.CANCELLED, reservation.getTicket().getTicketStatus());
		assertEquals(PaymentStatus.REFUNDED, reservation.getPayment().getPaymentStatus());

		assertEquals(ScreeningSeatStatus.FREE, seats.get(0).getScreeningSeatStatus());
		assertEquals(ScreeningSeatStatus.FREE, seats.get(1).getScreeningSeatStatus());

		verify(reservationsRepository).save(reservation);
		verify(ticketRepository).save(reservation.getTicket());
		verify(paymentRepository).save(reservation.getPayment());
		verify(screeningSeatRepository).saveAll(seats);
	}

	@Test
	void cancelReservation_whenCancelIsNotOnTime_throwsReservationCancelNotOnTimeException() {
		ScreeningEntity screening = screening(LocalDate.now().minusDays(1), TimeSlot.PRIME);
		ReservationEntity reservation = reservation(
				ReservationStatus.CREATED,
				PaymentStatus.UNPAID,
				TicketStatus.VALID,
				screening,
				screeningSeats(ScreeningSeatStatus.RESERVED)
		);

		mockExistingReservation(reservation);
		when(screeningService.getScreeningEntity(SCREENING_ID)).thenReturn(screening);

		assertThatThrownBy(() -> reservationsService.cancelReservation(RESERVATION_ID))
				.isInstanceOf(ReservationCancelNotOnTimeException.class)
				.hasMessage("Reservation with id " + RESERVATION_ID + " cannot be cancelled." +
						"Reservation must be cancelled at least 60 min before screening");

		verifyNoCancelWrites();
	}

	@Test
	void cancelReservation_whenStatusIsNotValid_throwsReservationInvalidStatusFlowException() {
		ScreeningEntity screening = screening(LocalDate.now().plusDays(1), TimeSlot.PRIME);
		ReservationEntity reservation = reservation(
				ReservationStatus.CANCELLED,
				PaymentStatus.REFUNDED,
				TicketStatus.CANCELLED,
				screening,
				screeningSeats(ScreeningSeatStatus.FREE)
		);

		mockExistingReservation(reservation);
		when(screeningService.getScreeningEntity(SCREENING_ID)).thenReturn(screening);

		assertThatThrownBy(() -> reservationsService.cancelReservation(RESERVATION_ID))
				.isInstanceOf(ReservationInvalidStatusFlowException.class)
				.hasMessage("Status of reservation with " + RESERVATION_ID +
						" cannot be changed to CANCELLED invalid order.");

		verifyNoCancelWrites();
	}

	@Test
	void completePayment_whenStatusIsValid_completesPaymentAndConfirmsReservation() {
		ReservationEntity reservation = reservation(
				ReservationStatus.CREATED,
				PaymentStatus.UNPAID,
				TicketStatus.VALID,
				screening(LocalDate.now().plusDays(1), TimeSlot.PRIME),
				screeningSeats(ScreeningSeatStatus.RESERVED)
		);

		when(reservationsRepository.findById(RESERVATION_ID)).thenReturn(Optional.of(reservation));
		when(paymentRepository.findByReservation_Id(RESERVATION_ID)).thenReturn(reservation.getPayment());

		reservationsService.completePayment(RESERVATION_ID);

		assertEquals(ReservationStatus.CONFIRMED, reservation.getReservationStatus());
		assertEquals(PaymentStatus.PAID, reservation.getPayment().getPaymentStatus());

		verify(reservationsRepository).save(reservation);
		verify(paymentRepository).save(reservation.getPayment());
		verify(ticketRepository, never()).save(any());
		verify(screeningSeatRepository, never()).saveAll(anyList());
	}

	@Test
	void completePayment_whenStatusIsInvalid_throwsReservationCompletePaymentException() {
		ReservationEntity reservation = reservation(
				ReservationStatus.CANCELLED,
				PaymentStatus.REFUNDED,
				TicketStatus.CANCELLED,
				screening(LocalDate.now().plusDays(1), TimeSlot.PRIME),
				screeningSeats(ScreeningSeatStatus.FREE)
		);

		when(reservationsRepository.findById(RESERVATION_ID)).thenReturn(Optional.of(reservation));
		when(paymentRepository.findByReservation_Id(RESERVATION_ID)).thenReturn(reservation.getPayment());

		assertThatThrownBy(() -> reservationsService.completePayment(RESERVATION_ID))
				.isInstanceOf(ReservationCompletePaymentException.class)
				.hasMessage("Payment for reservation with " + RESERVATION_ID + " id cannot be completed." +
						" Check status of reservation and payment");

		assertEquals(ReservationStatus.CANCELLED, reservation.getReservationStatus());
		assertEquals(PaymentStatus.REFUNDED, reservation.getPayment().getPaymentStatus());

		verify(reservationsRepository, never()).save(any());
		verify(paymentRepository, never()).save(any());
		verify(ticketRepository, never()).save(any());
	}

	@Test
	void setTicketToUsed_whenStatusIsValid_setsTicketToUsed() {
		TicketEntity ticket = ticket(
				TicketStatus.VALID,
				reservation(
						ReservationStatus.CONFIRMED,
						PaymentStatus.PAID,
						TicketStatus.VALID,
						screening(LocalDate.now().plusDays(1), TimeSlot.PRIME),
						screeningSeats(ScreeningSeatStatus.RESERVED)
				)
		);

		when(ticketRepository.findByTicketNumber(ticket.getTicketNumber()))
				.thenReturn(Optional.of(ticket));

		reservationsService.setTicketToUsed(ticket.getTicketNumber());

		assertEquals(TicketStatus.USED, ticket.getTicketStatus());

		verify(ticketRepository).save(ticket);
		verify(reservationsRepository, never()).save(any());
		verify(paymentRepository, never()).save(any());
	}

	@Test
	void setTicketToUsed_whenStatusIsInvalid_throwsTicketUseNotPossibleException() {
		TicketEntity ticket = ticket(
				TicketStatus.CANCELLED,
				reservation(
						ReservationStatus.CANCELLED,
						PaymentStatus.REFUNDED,
						TicketStatus.CANCELLED,
						screening(LocalDate.now().plusDays(1), TimeSlot.PRIME),
						screeningSeats(ScreeningSeatStatus.FREE)
				)
		);

		when(ticketRepository.findByTicketNumber(ticket.getTicketNumber()))
				.thenReturn(Optional.of(ticket));

		assertThatThrownBy(() -> reservationsService.setTicketToUsed(ticket.getTicketNumber()))
				.isInstanceOf(TicketUseNotPossibleException.class)
				.hasMessage("Ticket number " + ticket.getTicketNumber() + " cannot check in, check status");

		assertEquals(TicketStatus.CANCELLED, ticket.getTicketStatus());

		verify(ticketRepository, never()).save(any());
		verify(reservationsRepository, never()).save(any());
		verify(paymentRepository, never()).save(any());
	}

	@Test
	void setStatusOfExpiredTickets_whenExpiredTicketsExist_setsTicketsToExpired() {
		ReservationEntity reservation = reservation(
				ReservationStatus.CONFIRMED,
				PaymentStatus.PAID,
				TicketStatus.VALID,
				screening(LocalDate.now().minusDays(1), TimeSlot.PRIME),
				screeningSeats(ScreeningSeatStatus.RESERVED)
		);

		TicketEntity ticket1 = ticket(TicketStatus.VALID, reservation);
		TicketEntity ticket2 = ticket(TicketStatus.VALID, reservation);

		when(ticketRepository.getExpiredTickets(any(LocalDate.class), any(LocalTime.class)))
				.thenReturn(List.of(ticket1, ticket2));

		reservationsService.setStatusOfExpiredTickets();

		assertEquals(TicketStatus.EXPIRED, ticket1.getTicketStatus());
		assertEquals(TicketStatus.EXPIRED, ticket2.getTicketStatus());

		verify(ticketRepository).saveAll(List.of(ticket1, ticket2));
		verify(ticketRepository, never()).save(any());
		verify(reservationsRepository, never()).save(any());
		verify(paymentRepository, never()).save(any());
	}

	@Test
	void setStatusOfExpiredTickets_whenNoExpiredTicketsExist_doesNothing() {
		when(ticketRepository.getExpiredTickets(any(LocalDate.class), any(LocalTime.class)))
				.thenReturn(List.of());

		reservationsService.setStatusOfExpiredTickets();

		verify(ticketRepository, never()).saveAll(anyList());
		verify(ticketRepository, never()).save(any());
	}

	private ReservationRequest onlineReservationRequest() {
		return new ReservationRequest(SCREENING_ID, SEAT_IDS, PaymentMethod.ONLINE);
	}

	private ScreeningEntity screening(LocalDate date, TimeSlot timeSlot) {
		MovieEntity movie = new MovieEntity("testTitle", "testDescription", Genre.COMEDY, 10);
		movie.setId(1L);

		ScreeningEntity screening = new ScreeningEntity(
				1L,
				movie,
				date,
				timeSlot,
				PRICE_PER_SEAT
		);

		screening.setId(SCREENING_ID);
		screening.setScreeningDate(date);
		screening.setTimes(timeSlot);

		return screening;
	}

	private SeatEntity seat(long id, int rowNumber, int seatNumber) {
		SeatEntity seat = new SeatEntity();
		seat.setId(id);
		seat.setRowNumber(rowNumber);
		seat.setSeatNumber(seatNumber);
		return seat;
	}

	private ScreeningSeatEntity screeningSeat(
			long seatId,
			int rowNumber,
			int seatNumber,
			ScreeningSeatStatus status
	) {
		ScreeningSeatEntity screeningSeat = new ScreeningSeatEntity();
		screeningSeat.setCinemaSeats(seat(seatId, rowNumber, seatNumber));
		screeningSeat.setScreeningSeatStatus(status);
		return screeningSeat;
	}

	private List<ScreeningSeatEntity> screeningSeats(ScreeningSeatStatus status) {
		return List.of(
				screeningSeat(1L, 1, 1, status),
				screeningSeat(2L, 1, 2, status)
		);
	}

	private ReservationEntity reservation(
			ReservationStatus reservationStatus,
			PaymentStatus paymentStatus,
			TicketStatus ticketStatus,
			ScreeningEntity screening,
			List<ScreeningSeatEntity> seats
	) {
		ReservationEntity reservation = new ReservationEntity();
		reservation.setId(RESERVATION_ID);
		reservation.setReservationStatus(reservationStatus);
		reservation.setScreening(screening);
		reservation.setReservedSeats(seats);

		PaymentEntity payment = new PaymentEntity();
		payment.setId(PAYMENT_ID);
		payment.setReservation(reservation);
		payment.setPaymentStatus(paymentStatus);

		TicketEntity ticket = new TicketEntity();
		ticket.setId(TICKET_ID);
		ticket.setTicketNumber(UUID.randomUUID().toString());
		ticket.setReservation(reservation);
		ticket.setTicketStatus(ticketStatus);

		reservation.setPayment(payment);
		reservation.setTicket(ticket);

		return reservation;
	}

	private TicketEntity ticket(TicketStatus ticketStatus, ReservationEntity reservation) {
		TicketEntity ticket = reservation.getTicket();
		ticket.setTicketNumber(UUID.randomUUID().toString());
		ticket.setTicketStatus(ticketStatus);
		ticket.setReservation(reservation);

		reservation.setTicket(ticket);

		return ticket;
	}

	private void mockScreening(ScreeningEntity screening) {
		when(screeningService.getScreeningEntity(SCREENING_ID)).thenReturn(screening);
	}

	private void mockSeatsAreFree(ReservationRequest request, boolean result) {
		when(screeningSeatRepository.areAllCinemaRoomSeatsFree(
				eq(SCREENING_ID),
				eq(request.getCinemaRoomSeatIds()),
				eq((long) request.getCinemaRoomSeatIds().size())
		)).thenReturn(result);
	}

	private void mockScreeningSeats(ScreeningEntity screening, List<ScreeningSeatEntity> seats) {
		when(screeningSeatRepository.getScreeningSeatsByScreening(screening))
				.thenReturn(seats);
	}

	private void mockCreateReservationSaves() {
		when(reservationsRepository.save(any(ReservationEntity.class)))
				.thenAnswer(invocation -> {
					ReservationEntity reservation = invocation.getArgument(0);
					reservation.setId(RESERVATION_ID);
					return reservation;
				});

		when(paymentRepository.save(any(PaymentEntity.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		when(ticketRepository.save(any(TicketEntity.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		when(screeningSeatRepository.saveAll(anyList()))
				.thenAnswer(invocation -> invocation.getArgument(0));
	}

	private void mockExistingReservation(ReservationEntity reservation) {
		when(reservationsRepository.findById(RESERVATION_ID))
				.thenReturn(Optional.of(reservation));

		when(paymentRepository.findByReservation_Id(RESERVATION_ID))
				.thenReturn(reservation.getPayment());

		when(ticketRepository.findById(reservation.getTicket().getId()))
				.thenReturn(Optional.of(reservation.getTicket()));
	}

	private void verifyNoReservationCreation() {
		verify(reservationsRepository, never()).save(any());
		verify(paymentRepository, never()).save(any());
		verify(ticketRepository, never()).save(any());
		verify(screeningSeatRepository, never()).saveAll(anyList());
	}

	private void verifyNoCancelWrites() {
		verify(reservationsRepository, never()).save(any());
		verify(paymentRepository, never()).save(any());
		verify(ticketRepository, never()).save(any());
		verify(screeningSeatRepository, never()).saveAll(anyList());
	}
}
