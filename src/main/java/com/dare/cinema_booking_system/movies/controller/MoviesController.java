package com.dare.cinema_booking_system.movies.controller;

import com.dare.cinema_booking_system.movies.dto.MoviesRequest;
import com.dare.cinema_booking_system.movies.dto.MoviesResponse;
import com.dare.cinema_booking_system.movies.service.MoviesService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movies")
@AllArgsConstructor
public class MoviesController {

	private final MoviesService  moviesService;

	@GetMapping
	public ResponseEntity<Page<MoviesResponse>> getPageOfMovies(@PageableDefault(page = 0, size = 10,
												sort = "title",direction = Sort.Direction.ASC) Pageable pageable) {
		return ResponseEntity.ok(moviesService.getPageOfMovies(pageable));
	}

	@GetMapping("/{id}")
	public ResponseEntity<MoviesResponse> getMoviesById(@PathVariable Long id) {
		return ResponseEntity.ok(moviesService.getMovieResponseById(id));
	}

	@PostMapping
	public ResponseEntity<MoviesResponse> createMovies(@RequestBody @Valid MoviesRequest moviesRequest) {
		return ResponseEntity.status(HttpStatus.CREATED).body(moviesService.addMovies(moviesRequest));
	}

	@PatchMapping("/{id}")
	public ResponseEntity<MoviesResponse> updateMovies(@RequestBody @Valid MoviesRequest moviesRequest, @PathVariable Long id) {
		return ResponseEntity.status(HttpStatus.OK).body(moviesService.updateMovies(id, moviesRequest));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteMovies(@PathVariable Long id) {
		moviesService.deleteMovies(id);
		return ResponseEntity.noContent().build();
	}

}
