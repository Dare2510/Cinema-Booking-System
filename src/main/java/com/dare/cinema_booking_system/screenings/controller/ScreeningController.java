package com.dare.cinema_booking_system.screenings.controller;

import com.dare.cinema_booking_system.screenings.dto.ScreeningResponse;
import com.dare.cinema_booking_system.screenings.dto.ScreeningSeatResponse;
import com.dare.cinema_booking_system.screenings.service.ScreeningSeatService;
import com.dare.cinema_booking_system.screenings.service.ScreeningService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/screening")
@AllArgsConstructor
public class ScreeningController {

	private final ScreeningService screeningService;
	private final ScreeningSeatService screeningSeatService;


	@GetMapping("/upcoming")
	public ResponseEntity<List<ScreeningResponse>> getUpcomingScreenings() {
		return ResponseEntity.ok(screeningService.getUpcomingScreenings());
	}

	@GetMapping("/{screeningId}/seats/free")
	public ResponseEntity<List<ScreeningSeatResponse>> getFreeSeats(@PathVariable Long screeningId) {
		return ResponseEntity.ok(screeningSeatService.getFreeScreeningSeatsByScreeningId(screeningId));
	}


}
