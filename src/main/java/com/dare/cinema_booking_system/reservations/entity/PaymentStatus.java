package com.dare.cinema_booking_system.reservations.entity;

public enum PaymentStatus {
	PAID,
	UNPAID,
	REFUND_PENDING,
	REFUNDED;

	public boolean validatorToCompletePayment(ReservationStatus currentStatus, PaymentStatus currentPaymentStatus){
		return currentStatus == ReservationStatus.CREATED && currentPaymentStatus == PaymentStatus.UNPAID;
	}

	public boolean validatorToRefundPayment(ReservationStatus currentReservationStatus, PaymentStatus currentPaymentStatus){
		return currentPaymentStatus == PaymentStatus.REFUND_PENDING && currentReservationStatus == ReservationStatus.CANCELLED;
	}
}
