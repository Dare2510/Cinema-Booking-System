package com.dare.cinema_booking_system.reservations.entity;

import com.dare.cinema_booking_system.screenings.entity.ScreeningEntity;
import com.dare.cinema_booking_system.screenings.entity.ScreeningSeatEntity;
import com.dare.cinema_booking_system.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservation")
@Getter
@Setter
public class ReservationEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "created_at", nullable = false)
	LocalDateTime createdAt;

	@Column(name = "reservation_status", nullable = false)
	@Enumerated(EnumType.STRING)
	private ReservationStatus reservationStatus;

	@OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL)
	private PaymentEntity payment;

	@OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL)
	private TicketEntity ticket;

	@ManyToOne
	@JoinColumn(name = "screening_id")
	private ScreeningEntity screening;

	@ManyToMany
	@JoinTable(
			name = "reservation_screening_seats",
			joinColumns = @JoinColumn(name = "reservation_id"),
			inverseJoinColumns = @JoinColumn(name = "screening_seat_id")
	)
	private List<ScreeningSeatEntity> reservedSeats;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private UserEntity user;

	public ReservationEntity(PaymentEntity payment, TicketEntity ticket, ScreeningEntity screening) {
		this.createdAt = LocalDateTime.now();
		this.reservationStatus = ReservationStatus.CREATED;
		this.payment = payment;
		this.ticket = ticket;
		this.screening = screening;
		this.reservedSeats = new ArrayList<>();
	}

	public ReservationEntity() {
		this.createdAt = LocalDateTime.now();
		this.reservationStatus = ReservationStatus.CREATED;
		this.reservedSeats = new ArrayList<>();
	}
}
