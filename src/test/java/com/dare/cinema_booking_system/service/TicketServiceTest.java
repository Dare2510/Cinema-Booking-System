package com.dare.cinema_booking_system.service;

import com.dare.cinema_booking_system.reservations.entity.*;
import com.dare.cinema_booking_system.reservations.exceptions.TicketNotFoundException;
import com.dare.cinema_booking_system.reservations.exceptions.TicketUseNotPossibleException;
import com.dare.cinema_booking_system.reservations.repository.TicketRepository;
import com.dare.cinema_booking_system.reservations.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

	private static final Long TICKET_ID = 1L;
	private static final String TICKET_NUMBER = UUID.randomUUID().toString();

	private TicketService ticketService;

	@Mock
	private TicketRepository ticketRepository;

	@Mock
	private Clock clock;

	@BeforeEach
	void setUp() {
		ticketService = new TicketService(ticketRepository, clock);
	}

	@Test
	void setTicketToUsed_whenStatusIsValid_setsTicketToUsed() {
		TicketEntity ticket = ticket(TicketStatus.VALID, reservation(ReservationStatus.CONFIRMED, PaymentStatus.PAID));
		when(ticketRepository.findByTicketNumber(ticket.getTicketNumber())).thenReturn(Optional.of(ticket));

		ticketService.setTicketToUsed(ticket.getTicketNumber());

		assertEquals(TicketStatus.USED, ticket.getTicketStatus());
		verify(ticketRepository).save(ticket);
	}

	@Test
	void setTicketToUsed_whenStatusIsInvalid_throwsTicketUseNotPossibleException() {
		TicketEntity ticket = ticket(TicketStatus.CANCELLED, reservation(ReservationStatus.CANCELLED, PaymentStatus.REFUNDED));
		when(ticketRepository.findByTicketNumber(ticket.getTicketNumber())).thenReturn(Optional.of(ticket));

		assertThatThrownBy(() -> ticketService.setTicketToUsed(ticket.getTicketNumber()))
				.isInstanceOf(TicketUseNotPossibleException.class)
				.hasMessage("Ticket number " + ticket.getTicketNumber() + " cannot check in, check status");

		assertEquals(TicketStatus.CANCELLED, ticket.getTicketStatus());
		verify(ticketRepository, never()).save(any());
	}

	@Test
	void setTicketToUsed_whenTicketDoesNotExist_throwsTicketNotFoundException() {
		when(ticketRepository.findByTicketNumber(TICKET_NUMBER)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> ticketService.setTicketToUsed(TICKET_NUMBER))
				.isInstanceOf(TicketNotFoundException.class)
				.hasMessage("Ticket number " + TICKET_NUMBER + " not found");

		verify(ticketRepository, never()).save(any());
	}

	@Test
	void setStatusOfExpiredTickets_whenExpiredTicketsExist_setsTicketsToExpired() {
		TicketEntity ticket1 = ticket(TicketStatus.VALID, reservation(ReservationStatus.CONFIRMED, PaymentStatus.PAID));
		TicketEntity ticket2 = ticket(TicketStatus.VALID, reservation(ReservationStatus.CONFIRMED, PaymentStatus.PAID));
		LocalDateTime now = LocalDate.now().atTime(LocalTime.NOON);
		mockCurrentTime(now);

		when(ticketRepository.getExpiredTickets(any(LocalDate.class), any(LocalTime.class)))
				.thenReturn(List.of(ticket1, ticket2));

		ticketService.setStatusOfExpiredTickets();

		assertEquals(TicketStatus.EXPIRED, ticket1.getTicketStatus());
		assertEquals(TicketStatus.EXPIRED, ticket2.getTicketStatus());
		verify(ticketRepository).saveAll(List.of(ticket1, ticket2));
	}

	@Test
	void setStatusOfExpiredTickets_whenNoExpiredTicketsExist_doesNotSave() {
		LocalDateTime now = LocalDate.now().atTime(LocalTime.NOON);
		mockCurrentTime(now);
		when(ticketRepository.getExpiredTickets(any(LocalDate.class), any(LocalTime.class))).thenReturn(List.of());

		ticketService.setStatusOfExpiredTickets();

		verify(ticketRepository, never()).saveAll(anyList());
		verify(ticketRepository, never()).save(any());
	}

	@Test
	void createTicket_createsAndSavesTicket() {
		ReservationEntity reservation = reservation(ReservationStatus.CREATED, PaymentStatus.UNPAID);
		when(ticketRepository.save(any(TicketEntity.class)))
				.thenAnswer(invocation -> {
					TicketEntity ticket = invocation.getArgument(0);
					ticket.setId(TICKET_ID);
					return ticket;
				});

		TicketEntity ticket = ticketService.createTicket(reservation);

		assertNotNull(ticket.getTicketNumber());
		assertEquals(TICKET_ID, ticket.getId());
		assertEquals(TicketStatus.VALID, ticket.getTicketStatus());
		assertSame(reservation, ticket.getReservation());
		verify(ticketRepository).save(ticket);
	}

	@Test
	void findTicketById_whenTicketExists_returnsTicket() {
		TicketEntity ticket = ticket(TicketStatus.VALID, reservation(ReservationStatus.CONFIRMED, PaymentStatus.PAID));
		when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.of(ticket));

		TicketEntity result = ticketService.findTicketById(TICKET_ID);

		assertSame(ticket, result);
		verify(ticketRepository).findById(TICKET_ID);
	}

	@Test
	void findTicketById_whenTicketDoesNotExist_throwsTicketNotFoundException() {
		when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> ticketService.findTicketById(TICKET_ID))
				.isInstanceOf(TicketNotFoundException.class)
				.hasMessage("Ticket with id " + TICKET_ID + " not found");
	}

	private ReservationEntity reservation(ReservationStatus reservationStatus, PaymentStatus paymentStatus) {
		ReservationEntity reservation = new ReservationEntity();
		reservation.setReservationStatus(reservationStatus);

		PaymentEntity payment = new PaymentEntity(reservation, BigDecimal.valueOf(20), PaymentMethod.ONLINE);
		payment.setPaymentStatus(paymentStatus);
		reservation.setPayment(payment);
		return reservation;
	}

	private TicketEntity ticket(TicketStatus ticketStatus, ReservationEntity reservation) {
		TicketEntity ticket = new TicketEntity(UUID.randomUUID().toString(), reservation);
		ticket.setId(TICKET_ID);
		ticket.setTicketStatus(ticketStatus);
		reservation.setTicket(ticket);
		return ticket;
	}

	private void mockCurrentTime(LocalDateTime currentTime) {
		ZoneId zone = ZoneId.systemDefault();
		given(clock.instant()).willReturn(currentTime.atZone(zone).toInstant());
		given(clock.getZone()).willReturn(zone);
	}
}
