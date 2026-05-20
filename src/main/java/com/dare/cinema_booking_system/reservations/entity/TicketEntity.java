package com.dare.cinema_booking_system.reservations.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "tickets")
@Getter @Setter
@NoArgsConstructor
public class TicketEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "ticket_number", nullable = false)
	Long ticketNumber;

	@Enumerated(EnumType.STRING)
	@Column(name = "ticket_status", nullable = false)
	private TicketStatus ticketStatus;

	@Column(name = "date_issued", nullable = false)
	private LocalDate dateIssued;

	@ManyToOne
	@JoinColumn(name = "reservation_id")
	private ReservationEntity reservation;

	public TicketEntity(Long ticketNumber, TicketStatus ticketStatus, LocalDate dateIssued, ReservationEntity reservation) {
		this.ticketNumber = ticketNumber;
		this.ticketStatus = TicketStatus.VALID;
		this.dateIssued = LocalDate.now();
		this.reservation = reservation;
	}
}
