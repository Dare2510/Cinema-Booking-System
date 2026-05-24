package com.dare.cinema_booking_system.reservations.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class ReservationsResponse {

	public Long reservationId;
	public String ticketNumber;
	public LocalDate screeningDate;
	public List<String> reservedSeats;



}
