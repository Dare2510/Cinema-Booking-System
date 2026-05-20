package com.dare.cinema_booking_system.reservations.entity;

import com.dare.cinema_booking_system.screenings.entity.ScreeningsEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "reservation")
@Getter @Setter
@NoArgsConstructor
public class ReservationEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "created_at",nullable = false)
	LocalDateTime createdAt;

	@Column(name = "reservation_status", nullable = false)
	@Enumerated(EnumType.STRING)
	private ReservationStatus reservationStatus;

	@OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL)
	private List<PaymentEntity> payments;

	@OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL)
	private List<TicketEntity> tickets;

	@ManyToOne
	@JoinColumn(name = "screening_id")
	private ScreeningsEntity screening;

	public ReservationEntity(LocalDateTime createdAt, ReservationStatus reservationStatus,
							 List<PaymentEntity> payments, List<TicketEntity> tickets, ScreeningsEntity screening) {
		this.createdAt = createdAt;
		this.reservationStatus = reservationStatus;
		this.payments = payments;
		this.tickets = tickets;
		this.screening = screening;
	}
}
