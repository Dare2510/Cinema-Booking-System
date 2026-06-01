package com.dare.cinema_booking_system.reservations.dto;

import com.dare.cinema_booking_system.screenings.entity.TimeSlot;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReservationResponse {

	public Long reservationId;
	public String ticketNumber;
	public LocalDate screeningDate;
	public TimeSlot timeSlot;
	public List<String> reservedSeats;
	public PaymentResponse paymentResponse;



}
