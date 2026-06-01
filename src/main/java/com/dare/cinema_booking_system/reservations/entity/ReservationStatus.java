package com.dare.cinema_booking_system.reservations.entity;

public enum ReservationStatus {
	CREATED,
	CONFIRMED,
	CANCELLED;

	public boolean correctStatusOrder(ReservationEntity toChange, ReservationStatus status) {
		boolean validStatusChange = false;
		switch (toChange.getReservationStatus()) {
			case CREATED:
				if (status == CONFIRMED || status == CANCELLED) {
					validStatusChange = true;
				}
				break;
			case CONFIRMED:
				if (status == CANCELLED) {
					validStatusChange = true;
				}
		}
		return validStatusChange;
	}
}
