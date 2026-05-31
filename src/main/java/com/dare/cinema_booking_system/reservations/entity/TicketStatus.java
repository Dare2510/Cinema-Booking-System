package com.dare.cinema_booking_system.reservations.entity;

public enum TicketStatus {
	VALID,
	CANCELLED,
	USED,
	EXPIRED;

	public boolean validatorForUsed(TicketStatus currentStatus, PaymentStatus currentPaymentStatus) {
		return currentStatus == VALID && currentPaymentStatus == PaymentStatus.PAID;
	}
}
