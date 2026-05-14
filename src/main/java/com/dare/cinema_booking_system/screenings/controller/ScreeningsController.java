package com.dare.cinema_booking_system.screenings.controller;

import com.dare.cinema_booking_system.screenings.dto.ScreeningsRequest;
import com.dare.cinema_booking_system.screenings.dto.ScreeningsResponse;
import com.dare.cinema_booking_system.screenings.service.ScreeningsService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/screening")
@AllArgsConstructor
public class ScreeningsController {

	private final ScreeningsService screeningsService;

	@PostMapping
	public ResponseEntity<ScreeningsResponse> createScreening(@RequestBody @Valid ScreeningsRequest screeningsRequest) {
		return ResponseEntity.ok(screeningsService.createScreenings(screeningsRequest));

	}
	@GetMapping
	public ResponseEntity<Page<ScreeningsResponse>> getPageOfScreenings(@PageableDefault(page = 0, size = 10,
			sort = "screeningDate", direction = Sort.Direction.ASC)Pageable pageable) {
		return ResponseEntity.ok(screeningsService.getPageOfScreenings(pageable));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ScreeningsResponse> getScreeningById(@PathVariable long id) {
		return ResponseEntity.ok(screeningsService.getScreeningById(id));
	}
	@DeleteMapping("{id}")
	public ResponseEntity<Void> deleteScreeningById(@PathVariable long id) {
		screeningsService.deleteScreeningById(id);
		return ResponseEntity.noContent().build();
	}
}
