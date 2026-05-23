package com.dare.cinema_booking_system.reservations.controller;

import com.dare.cinema_booking_system.reservations.service.ReservationsService;
import com.dare.cinema_booking_system.screenings.dto.ScreeningSeatResponse;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reservation/")
@AllArgsConstructor
public class ReservationsController {

	private final ReservationsService reservationsService;

	@GetMapping("{screeningId}")
	public ResponseEntity<List<ScreeningSeatResponse>> getFreeSeats(@PathVariable Long screeningId) {
		return ResponseEntity.ok(reservationsService.getFreeScreeningSeatsByScreeningId(screeningId));
	}
}
