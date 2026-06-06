package com.dare.cinema_booking_system.reservations.controller;

import com.dare.cinema_booking_system.reservations.dto.ReservationRequest;
import com.dare.cinema_booking_system.reservations.dto.ReservationResponse;
import com.dare.cinema_booking_system.reservations.service.ReservationsService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservation/")
@AllArgsConstructor
public class ReservationController {

	private final ReservationsService reservationsService;

	@GetMapping("/{reservationId}")
	public ResponseEntity<ReservationResponse> getReservationById(@PathVariable Long reservationId) {
		return ResponseEntity.ok(reservationsService.findReservationById(reservationId));
	}

	@PostMapping
	public ResponseEntity<ReservationResponse> createReservations(@Valid @RequestBody ReservationRequest reservationRequest) {
		return ResponseEntity.ok(reservationsService.createReservation(reservationRequest));
	}

	@PatchMapping("/{reservationId}/cancel")
	public ResponseEntity<Void> cancelReservations(@PathVariable Long reservationId) {
		reservationsService.cancelReservation(reservationId);
		return ResponseEntity.ok().build();
	}


}
