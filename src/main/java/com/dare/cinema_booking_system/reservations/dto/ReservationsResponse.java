package com.dare.cinema_booking_system.reservations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class ReservationsResponse {

	public final Long reservationId;
	public final Long ticketNumber;
	public final Long screeningDate;
	public final String reservedSeats;



}
