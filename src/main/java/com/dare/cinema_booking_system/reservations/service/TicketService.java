package com.dare.cinema_booking_system.reservations.service;

import com.dare.cinema_booking_system.reservations.entity.PaymentStatus;
import com.dare.cinema_booking_system.reservations.entity.ReservationEntity;
import com.dare.cinema_booking_system.reservations.entity.TicketEntity;
import com.dare.cinema_booking_system.reservations.entity.TicketStatus;
import com.dare.cinema_booking_system.reservations.exceptions.TicketNotFoundException;
import com.dare.cinema_booking_system.reservations.exceptions.TicketUseNotPossibleException;
import com.dare.cinema_booking_system.reservations.repository.TicketRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@AllArgsConstructor
public class TicketService {

	private final TicketRepository ticketRepository;
	private final Clock clock;


	public void setTicketToUsed(String ticketNumber) {
		TicketEntity ticket = getTicketByNumber(ticketNumber);
		TicketStatus ticketStatus = ticket.getTicketStatus();
		PaymentStatus paymentStatus = ticket.getReservation().getPayment().getPaymentStatus();

		//Validator to check if the ticket can be used, ticket status must be valid and payment status paid
		boolean validStatus = ticketStatus.validatorForUsed(ticketStatus, paymentStatus);

		if (validStatus) {
			ticket.setTicketStatus(TicketStatus.USED);
			ticketRepository.save(ticket);
		} else {
			log.warn("{} has wrong status, ticket status: {}, payment status: {} ", ticketNumber, ticketStatus, paymentStatus);
			throw new TicketUseNotPossibleException(ticketNumber);
		}


	}

	//Sets expired tickets to status "expired"
	public void setStatusOfExpiredTickets() {
		LocalDate dateNow = LocalDate.now(clock);
		LocalTime timeNow = LocalTime.now(clock);

		List<TicketEntity> expiredTickets = ticketRepository.getExpiredTickets(dateNow, timeNow);
		if (!expiredTickets.isEmpty()) {
			expiredTickets.forEach(ticket -> {
				log.info("Ticket with ID {} has been expired", ticket.getId());
				ticket.setTicketStatus(TicketStatus.EXPIRED);
			});
			ticketRepository.saveAll(expiredTickets);
		} else {
			log.info("No expired tickets found");
		}

	}

	//Helper Methods

	public TicketEntity createTicket(ReservationEntity newReservation) {
		String ticketNumber = UUID.randomUUID().toString();

		TicketEntity ticket = new TicketEntity(ticketNumber, newReservation);
		ticketRepository.save(ticket);
		log.info("Ticket with id {} has been created", ticket.getId());

		return ticket;
	}

	public TicketEntity findTicketById(Long ticketId) {
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

	public void saveTicket(TicketEntity ticket) {
		ticketRepository.save(ticket);
	}
}
