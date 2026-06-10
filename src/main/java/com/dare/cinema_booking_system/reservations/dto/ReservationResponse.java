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

	private Long reservationId;
	private String ticketNumber;
	private LocalDate screeningDate;
	private TimeSlot timeSlot;
	private List<String> reservedSeats;
	private PaymentResponse paymentResponse;


}
