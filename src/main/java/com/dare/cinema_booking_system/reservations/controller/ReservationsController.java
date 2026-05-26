package com.dare.cinema_booking_system.reservations.controller;

import com.dare.cinema_booking_system.reservations.dto.ReservationsRequest;
import com.dare.cinema_booking_system.reservations.dto.ReservationsResponse;
import com.dare.cinema_booking_system.reservations.service.ReservationsService;
import com.dare.cinema_booking_system.screenings.dto.ScreeningSeatResponse;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservation/")
@AllArgsConstructor
public class ReservationsController {

	private final ReservationsService reservationsService;

	@GetMapping("seats/{screeningId}")
	public ResponseEntity<List<ScreeningSeatResponse>> getFreeSeats(@PathVariable Long screeningId) {
		return ResponseEntity.ok(reservationsService.getFreeScreeningSeatsByScreeningId(screeningId));
	}

	@GetMapping("/{screeningId}")
	public ResponseEntity<ReservationsResponse> getScreeningById(@PathVariable Long screeningId) {
		return ResponseEntity.ok(reservationsService.findReservationById(screeningId));
	}
	@PostMapping
	public ResponseEntity<ReservationsResponse> createReservations(@RequestBody ReservationsRequest reservationsRequest) {
		return ResponseEntity.ok(reservationsService.createReservation(reservationsRequest));
	}
	@PatchMapping("/cancel/{reservationId}")
	public ResponseEntity<Void> cancelReservations(@PathVariable Long reservationId) {
		reservationsService.cancelReservation(reservationId);
		return ResponseEntity.ok().build();
	}
}
