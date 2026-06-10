package com.dare.cinema_booking_system.screenings.controller;

import com.dare.cinema_booking_system.screenings.dto.ScreeningRequest;
import com.dare.cinema_booking_system.screenings.dto.ScreeningResponse;
import com.dare.cinema_booking_system.screenings.dto.ScreeningSeatResponse;
import com.dare.cinema_booking_system.screenings.service.ScreeningSeatService;
import com.dare.cinema_booking_system.screenings.service.ScreeningService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/management/screening")
@AllArgsConstructor
public class ScreeningManagementController {

	private final ScreeningService screeningService;
	private final ScreeningSeatService screeningSeatService;

	@GetMapping
	public ResponseEntity<Page<ScreeningResponse>> getPageOfScreenings(@PageableDefault(page = 0, size = 10,
			sort = "screeningDate", direction = Sort.Direction.ASC) Pageable pageable) {
		return ResponseEntity.ok(screeningService.getPageOfScreenings(pageable));
	}

	@PostMapping
	public ResponseEntity<ScreeningResponse> createScreening(@RequestBody @Valid ScreeningRequest screeningRequest) {
		return ResponseEntity.ok(screeningService.createScreening(screeningRequest));

	}

	@GetMapping("/upcoming")
	public ResponseEntity<List<ScreeningResponse>> getUpcomingScreenings() {
		return ResponseEntity.ok(screeningService.getUpcomingScreenings());
	}

	@GetMapping("/{id}")
	public ResponseEntity<ScreeningResponse> getScreeningById(@PathVariable Long id) {
		return ResponseEntity.ok(screeningService.getScreeningById(id));
	}

	@GetMapping("/{screeningId}/seats/free")
	public ResponseEntity<List<ScreeningSeatResponse>> getFreeSeats(@PathVariable Long screeningId) {
		return ResponseEntity.ok(screeningSeatService.getFreeScreeningSeatsByScreeningId(screeningId));
	}

	@PatchMapping("/{id}")
	public ResponseEntity<ScreeningResponse> updateScreening(@PathVariable Long id, @RequestBody @Valid ScreeningRequest screeningRequest) {
		return ResponseEntity.ok(screeningService.updateScreening(id, screeningRequest));
	}

	@DeleteMapping("{id}")
	public ResponseEntity<Void> deleteScreeningById(@PathVariable Long id) {
		screeningService.deleteScreeningById(id);
		return ResponseEntity.noContent().build();
	}


}
