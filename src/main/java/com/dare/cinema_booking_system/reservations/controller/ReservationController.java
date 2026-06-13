package com.dare.cinema_booking_system.reservations.controller;

import com.dare.cinema_booking_system.reservations.dto.ReservationRequest;
import com.dare.cinema_booking_system.reservations.dto.ReservationResponse;
import com.dare.cinema_booking_system.reservations.service.ReservationService;

import com.dare.cinema_booking_system.security.jwt.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservation")
@PreAuthorize("hasRole('USER')")
@AllArgsConstructor
public class ReservationController {

	private final ReservationService reservationService;

	@GetMapping("/{reservationId}")
	public ResponseEntity<ReservationResponse> getReservationById(@AuthenticationPrincipal AuthenticatedUser authenticatedUser, @PathVariable Long reservationId) {
		return ResponseEntity.ok(reservationService.findReservationById(authenticatedUser, reservationId));
	}

	@PostMapping
	public ResponseEntity<ReservationResponse> createReservation(@AuthenticationPrincipal AuthenticatedUser authenticatedUser, @Valid @RequestBody ReservationRequest reservationRequest) {
		return ResponseEntity.ok(reservationService.createReservation(authenticatedUser, reservationRequest));
	}

	@PatchMapping("/{reservationId}/cancel")
	public ResponseEntity<Void> cancelReservation(@AuthenticationPrincipal AuthenticatedUser authenticatedUser, @PathVariable Long reservationId) {
		reservationService.cancelReservation(authenticatedUser, reservationId);
		return ResponseEntity.ok().build();
	}


}
