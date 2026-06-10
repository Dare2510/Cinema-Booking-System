package com.dare.cinema_booking_system.reservations.controller;

import com.dare.cinema_booking_system.reservations.dto.ReservationRequest;
import com.dare.cinema_booking_system.reservations.dto.ReservationResponse;
import com.dare.cinema_booking_system.reservations.service.ReservationService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservation/")
@AllArgsConstructor
public class ReservationController {

	private final ReservationService reservationService;

	@GetMapping("/{reservationId}")
	public ResponseEntity<ReservationResponse> getReservationById(@PathVariable Long reservationId) {
		return ResponseEntity.ok(reservationService.findReservationById(reservationId));
	}

	@PostMapping
	public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody ReservationRequest reservationRequest) {
		return ResponseEntity.ok(reservationService.createReservation(reservationRequest));
	}

	@PatchMapping("/{reservationId}/cancel")
	public ResponseEntity<Void> cancelReservation(@PathVariable Long reservationId) {
		reservationService.cancelReservation(reservationId);
		return ResponseEntity.ok().build();
	}


}
