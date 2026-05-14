package com.dare.cinema_booking_system.screenings.controller;

import com.dare.cinema_booking_system.screenings.dto.ScreeningsRequest;
import com.dare.cinema_booking_system.screenings.dto.ScreeningsResponse;
import com.dare.cinema_booking_system.screenings.service.ScreeningsService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/screening")
@AllArgsConstructor
public class ScreeningsController {

	private final ScreeningsService screeningsService;

	@PostMapping
	public ResponseEntity<ScreeningsResponse> createScreening(@RequestBody @Valid ScreeningsRequest screeningsRequest) {
		return ResponseEntity.ok(screeningsService.createScreenings(screeningsRequest));

	}
}
