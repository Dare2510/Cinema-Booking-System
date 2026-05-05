package com.dare.cinema_booking_system.rooms.controller;

import com.dare.cinema_booking_system.movies.dto.MovieResponse;
import com.dare.cinema_booking_system.rooms.dto.CinemaRoomRequest;
import com.dare.cinema_booking_system.rooms.dto.CinemaRoomResponse;
import com.dare.cinema_booking_system.rooms.service.CinemaRoomService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rooms")
@AllArgsConstructor
public class CinemaRoomController {
	private CinemaRoomService cinemaRoomService;

	@PostMapping
	public ResponseEntity<CinemaRoomResponse> createRoom(@RequestBody CinemaRoomRequest cinemaRoomRequest){

		return ResponseEntity.ok().body(cinemaRoomService.createCinemaRoom(cinemaRoomRequest));
	}
}
