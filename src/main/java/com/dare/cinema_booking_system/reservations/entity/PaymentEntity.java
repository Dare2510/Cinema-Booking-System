package com.dare.cinema_booking_system.reservations.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "payment")
@Getter @Setter
@NoArgsConstructor
public class PaymentEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "amount", nullable = false)
	BigDecimal amount;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_status", nullable = false)
	PaymentStatus paymentStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_method", nullable = false)
	PaymentMethod paymentMethod;

	@ManyToOne
	@JoinColumn(name = "reservation_id")
	private ReservationEntity reservation;

	public PaymentEntity(ReservationEntity reservationEntity,BigDecimal amount,PaymentMethod paymentMethod) {
		this.reservation = reservationEntity;
		this.amount = amount;
		this.paymentStatus = PaymentStatus.UNPAID;
		this.paymentMethod = paymentMethod;
	}
}
