package com.dare.cinema_booking_system.service;

import com.dare.cinema_booking_system.movie.entity.Genre;
import com.dare.cinema_booking_system.movie.entity.MovieEntity;
import com.dare.cinema_booking_system.reservations.dto.ReservationRequest;
import com.dare.cinema_booking_system.reservations.dto.ReservationResponse;
import com.dare.cinema_booking_system.reservations.entity.*;
import com.dare.cinema_booking_system.reservations.exceptions.ReservationCancelNotOnTimeException;
import com.dare.cinema_booking_system.reservations.exceptions.ReservationInvalidStatusFlowException;
import com.dare.cinema_booking_system.reservations.repository.ReservationsRepository;
import com.dare.cinema_booking_system.reservations.service.PaymentService;
import com.dare.cinema_booking_system.reservations.service.ReservationService;
import com.dare.cinema_booking_system.reservations.service.TicketService;
import com.dare.cinema_booking_system.rooms.entity.SeatEntity;
import com.dare.cinema_booking_system.screenings.entity.ScreeningEntity;
import com.dare.cinema_booking_system.screenings.entity.ScreeningSeatEntity;
import com.dare.cinema_booking_system.screenings.entity.ScreeningSeatStatus;
import com.dare.cinema_booking_system.screenings.entity.TimeSlot;
import com.dare.cinema_booking_system.screenings.exceptions.ScreeningSeatNotAvailableException;
import com.dare.cinema_booking_system.screenings.repository.ScreeningSeatRepository;
import com.dare.cinema_booking_system.screenings.service.ScreeningSeatService;
import com.dare.cinema_booking_system.screenings.service.ScreeningService;
import com.dare.cinema_booking_system.security.principal.AuthenticatedUser;
import com.dare.cinema_booking_system.user.entity.Role;
import com.dare.cinema_booking_system.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

	private static final Long RESERVATION_ID = 1L;
	private static final Long SCREENING_ID = 1L;
	private static final Long TICKET_ID = 1L;
	private static final Long PAYMENT_ID = 1L;

	private static final List<Long> SEAT_IDS = List.of(1L, 2L);
	private static final BigDecimal PRICE_PER_SEAT = BigDecimal.valueOf(10);
	private static final TimeSlot SCREENING_TIMESLOT = TimeSlot.PRIME;

	private ReservationService reservationService;

	@Mock
	private ReservationsRepository reservationsRepository;

	@Mock
	private ScreeningSeatService screeningSeatService;

	@Mock
	private ScreeningService screeningService;

	@Mock
	private PaymentService paymentService;

	@Mock
	private TicketService ticketService;

	@Mock
	private UserService userService;

	@Mock
	private Clock clock;

	@Mock
	private ScreeningSeatRepository screeningSeatRepository;

	@BeforeEach
	void setUp() {
		reservationService = new ReservationService(
				reservationsRepository,
				screeningSeatService,
				screeningSeatRepository,
				screeningService,
				paymentService,
				ticketService,
				userService,
				clock


		);
	}

	@Test
	void createReservation_whenReservedSeatsAreFree_returnsReservationResponse() {
		ScreeningEntity screening = screening(LocalDate.now(), TimeSlot.PRIME);
		List<ScreeningSeatEntity> seats = screeningSeats(ScreeningSeatStatus.FREE);
		ReservationRequest request = onlineReservationRequest();
		AuthenticatedUser user = authenticatedAdmin();

		mockReservationSaveAssignsId();
		when(screeningService.getScreeningEntity(SCREENING_ID)).thenReturn(screening);
		when(screeningSeatService.seatsAreFree(screening, request)).thenReturn(true);
		when(paymentService.createPayment(eq(request), eq(screening), any(ReservationEntity.class)))
				.thenAnswer(invocation -> payment(invocation.getArgument(2), PaymentStatus.UNPAID, PaymentMethod.ONLINE));
		when(ticketService.createTicket(any(ReservationEntity.class)))
				.thenAnswer(invocation -> ticket(TicketStatus.VALID, invocation.getArgument(0)));
		when(screeningSeatService.seatStatusUpdater(screening, request)).thenReturn(seats);

		ReservationResponse response = reservationService.createReservation(user, request);

		assertNotNull(response);
		assertEquals(RESERVATION_ID, response.getReservationId());
		assertEquals(screening.getScreeningDate(), response.getScreeningDate());
		assertNotNull(response.getTicketNumber());

		assertEquals(2, response.getReservedSeats().size());
		assertTrue(response.getReservedSeats().contains("Row: 1 - Seat: 1"));
		assertTrue(response.getReservedSeats().contains("Row: 1 - Seat: 2"));

		assertNotNull(response.getPaymentResponse());
		assertEquals(0, BigDecimal.valueOf(20).compareTo(response.getPaymentResponse().getAmount()));
		assertEquals(PaymentStatus.UNPAID, response.getPaymentResponse().getPaymentStatus());

		verify(screeningService).getScreeningEntity(SCREENING_ID);
		verify(screeningSeatService).seatsAreFree(screening, request);
		verify(paymentService).createPayment(eq(request), eq(screening), any(ReservationEntity.class));
		verify(ticketService).createTicket(any(ReservationEntity.class));
		verify(screeningSeatService).seatStatusUpdater(screening, request);
		verify(reservationsRepository, times(2)).save(any(ReservationEntity.class));
	}

	@Test
	void createReservation_whenSeatsAreAlreadyReserved_throwsScreeningSeatNotAvailableException() {
		ScreeningEntity screening = screening(LocalDate.now(), TimeSlot.PRIME);
		ReservationRequest request = onlineReservationRequest();
		AuthenticatedUser user = authenticatedAdmin();

		when(screeningService.getScreeningEntity(SCREENING_ID)).thenReturn(screening);
		when(screeningSeatService.seatsAreFree(screening, request)).thenReturn(false);

		assertThatThrownBy(() -> reservationService.createReservation(user, request))
				.isInstanceOf(ScreeningSeatNotAvailableException.class)
				.hasMessage("Chosen seats are not available");

		verify(screeningService).getScreeningEntity(SCREENING_ID);
		verify(screeningSeatService).seatsAreFree(screening, request);
		verifyNoReservationCreation();
	}

	@Test
	void cancelReservation_whenCancelIsOnTimeAndStatusValid_cancelsReservation() {
		ScreeningEntity screening = screening(LocalDate.now().plusDays(1), TimeSlot.PRIME);
		List<ScreeningSeatEntity> seats = screeningSeats(ScreeningSeatStatus.RESERVED);
		AuthenticatedUser user = authenticatedAdmin();
		ReservationEntity reservation = reservation(
				ReservationStatus.CREATED,
				PaymentStatus.UNPAID,
				TicketStatus.VALID,
				screening,
				seats
		);

		mockExistingReservationForCancel(reservation);
		when(screeningService.getScreeningEntity(SCREENING_ID)).thenReturn(screening);
		mockCurrentTime(screening.getScreeningDate().atTime(SCREENING_TIMESLOT.getStartTime()).minusMinutes(90));
		doAnswer(invocation -> {
			PaymentEntity payment = invocation.getArgument(1);
			payment.setPaymentStatus(PaymentStatus.REFUNDED);
			return null;
		}).when(paymentService).statusUpdateCancelPayment(ReservationStatus.CREATED, reservation.getPayment());

		reservationService.cancelReservation(user, RESERVATION_ID);

		assertEquals(ReservationStatus.CANCELLED, reservation.getReservationStatus());
		assertEquals(TicketStatus.CANCELLED, reservation.getTicket().getTicketStatus());
		assertEquals(PaymentStatus.REFUNDED, reservation.getPayment().getPaymentStatus());
		assertEquals(ScreeningSeatStatus.FREE, seats.get(0).getScreeningSeatStatus());
		assertEquals(ScreeningSeatStatus.FREE, seats.get(1).getScreeningSeatStatus());

		verify(reservationsRepository).save(reservation);
		verify(ticketService).saveTicket(reservation.getTicket());
		verify(screeningSeatRepository).saveAll(seats);
		verify(paymentService).statusUpdateCancelPayment(ReservationStatus.CREATED, reservation.getPayment());
	}

	@Test
	void cancelReservation_whenCancelIsNotOnTime_throwsReservationCancelNotOnTimeException() {
		ScreeningEntity screening = screening(LocalDate.now(), TimeSlot.PRIME);
		AuthenticatedUser user = authenticatedAdmin();
		ReservationEntity reservation = reservation(
				ReservationStatus.CREATED,
				PaymentStatus.UNPAID,
				TicketStatus.VALID,
				screening,
				screeningSeats(ScreeningSeatStatus.RESERVED)
		);

		mockExistingReservationForCancel(reservation);
		when(screeningService.getScreeningEntity(SCREENING_ID)).thenReturn(screening);
		mockCurrentTime(screening.getScreeningDate().atTime(SCREENING_TIMESLOT.getStartTime()).minusMinutes(30));

		assertThatThrownBy(() -> reservationService.cancelReservation(user, RESERVATION_ID))
				.isInstanceOf(ReservationCancelNotOnTimeException.class)
				.hasMessage("Reservation with id " + RESERVATION_ID + " cannot be cancelled." +
						"Reservation must be cancelled at least 60 min before screening");

		verifyNoCancelWrites();
	}

	@Test
	void cancelReservation_whenStatusIsNotValid_throwsReservationInvalidStatusFlowException() {
		ScreeningEntity screening = screening(LocalDate.now().plusDays(1), TimeSlot.PRIME);
		AuthenticatedUser user = authenticatedAdmin();
		ReservationEntity reservation = reservation(
				ReservationStatus.CANCELLED,
				PaymentStatus.REFUNDED,
				TicketStatus.CANCELLED,
				screening,
				screeningSeats(ScreeningSeatStatus.FREE)
		);

		mockExistingReservationForCancel(reservation);
		when(screeningService.getScreeningEntity(SCREENING_ID)).thenReturn(screening);
		mockCurrentTime(screening.getScreeningDate().atTime(SCREENING_TIMESLOT.getStartTime()).minusMinutes(90));

		assertThatThrownBy(() -> reservationService.cancelReservation(user, RESERVATION_ID))
				.isInstanceOf(ReservationInvalidStatusFlowException.class)
				.hasMessage("Status of reservation with " + RESERVATION_ID +
						" cannot be changed to CANCELLED invalid order.");

		verifyNoCancelWrites();
	}

	private ReservationRequest onlineReservationRequest() {
		return new ReservationRequest(SCREENING_ID, SEAT_IDS, PaymentMethod.ONLINE);
	}

	private ScreeningEntity screening(LocalDate date, TimeSlot timeSlot) {
		MovieEntity movie = new MovieEntity("testTitle", "testDescription", Genre.COMEDY, 10);
		movie.setId(1L);

		ScreeningEntity screening = new ScreeningEntity(1L, movie, date, timeSlot, PRICE_PER_SEAT);
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

	private ScreeningSeatEntity screeningSeat(long seatId, int rowNumber, int seatNumber, ScreeningSeatStatus status) {
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

		PaymentEntity payment = payment(reservation, paymentStatus, PaymentMethod.ONLINE);
		TicketEntity ticket = ticket(ticketStatus, reservation);

		reservation.setPayment(payment);
		reservation.setTicket(ticket);
		return reservation;
	}

	private PaymentEntity payment(ReservationEntity reservation, PaymentStatus paymentStatus, PaymentMethod paymentMethod) {
		PaymentEntity payment = new PaymentEntity(reservation, BigDecimal.valueOf(20), paymentMethod);
		payment.setId(PAYMENT_ID);
		payment.setPaymentStatus(paymentStatus);
		return payment;
	}

	private TicketEntity ticket(TicketStatus ticketStatus, ReservationEntity reservation) {
		TicketEntity ticket = new TicketEntity(UUID.randomUUID().toString(), reservation);
		ticket.setId(TICKET_ID);
		ticket.setTicketStatus(ticketStatus);
		return ticket;
	}

	private AuthenticatedUser authenticatedAdmin() {
		return new AuthenticatedUser(99L, "Admin@gmail.com", Role.ADMIN);
	}

	private void mockReservationSaveAssignsId() {
		when(reservationsRepository.save(any(ReservationEntity.class)))
				.thenAnswer(invocation -> {
					ReservationEntity reservation = invocation.getArgument(0);
					reservation.setId(RESERVATION_ID);
					return reservation;
				});
	}

	private void mockExistingReservationForCancel(ReservationEntity reservation) {
		when(reservationsRepository.findById(RESERVATION_ID)).thenReturn(Optional.of(reservation));
		when(paymentService.findPaymentByReservationId(RESERVATION_ID)).thenReturn(reservation.getPayment());
		when(ticketService.findTicketById(reservation.getTicket().getId())).thenReturn(reservation.getTicket());
	}

	private void verifyNoReservationCreation() {
		verify(reservationsRepository, never()).save(any());
		verify(paymentService, never()).createPayment(any(), any(), any());
		verify(ticketService, never()).createTicket(any());
		verify(screeningSeatService, never()).seatStatusUpdater(any(), any());
	}

	private void verifyNoCancelWrites() {
		verify(reservationsRepository, never()).save(any());
		verify(ticketService, never()).saveTicket(any());
		verify(screeningSeatRepository, never()).saveAll(anyList());
		verify(paymentService, never()).statusUpdateCancelPayment(any(), any());
	}

	private void mockCurrentTime(LocalDateTime currentTime) {
		ZoneId zone = ZoneId.systemDefault();
		given(clock.instant()).willReturn(currentTime.atZone(zone).toInstant());
		given(clock.getZone()).willReturn(zone);
	}
}
