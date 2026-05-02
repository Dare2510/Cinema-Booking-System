package com.dare.cinema_booking_system.movies.controller;

import com.dare.cinema_booking_system.movies.dto.MovieRequest;
import com.dare.cinema_booking_system.movies.dto.MovieResponse;
import com.dare.cinema_booking_system.movies.entity.Genre;
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

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@AllArgsConstructor
public class MovieController {

	private final MoviesService  moviesService;

	@GetMapping
	public ResponseEntity<Page<MovieResponse>> getPageOfMovies(@PageableDefault(page = 0, size = 10,
												sort = "title",direction = Sort.Direction.ASC) Pageable pageable) {
		return ResponseEntity.ok(moviesService.getPageOfMovies(pageable));
	}

	@GetMapping("/{id}")
	public ResponseEntity<MovieResponse> getMoviesById(@PathVariable Long id) {
		return ResponseEntity.ok(moviesService.getMovieResponseById(id));
	}

	@GetMapping("/filter/duration/{duration}")
	public ResponseEntity<List<MovieResponse>> getMoviesByDuration(@PathVariable int duration) {
		return ResponseEntity.ok(moviesService.getListOfByDuration(duration));
	}

	@GetMapping("/filter/genre/{genre}")
	public ResponseEntity<List<MovieResponse>> getMoviesByDuration(@PathVariable Genre genre) {
		return ResponseEntity.ok(moviesService.getListOfByGenre(genre));
	}

	@PostMapping
	public ResponseEntity<MovieResponse> createMovies(@RequestBody @Valid MovieRequest movieRequest) {
		return ResponseEntity.status(HttpStatus.CREATED).body(moviesService.addMovies(movieRequest));
	}

	@PatchMapping("/{id}")
	public ResponseEntity<MovieResponse> updateMovies(@RequestBody @Valid MovieRequest movieRequest, @PathVariable Long id) {
		return ResponseEntity.status(HttpStatus.OK).body(moviesService.updateMovies(id, movieRequest));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteMovies(@PathVariable Long id) {
		moviesService.deleteMovies(id);
		return ResponseEntity.noContent().build();
	}


}
