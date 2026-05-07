package com.dare.cinema_booking_system.rooms.controller;

import com.dare.cinema_booking_system.rooms.dto.CinemaRoomRequest;
import com.dare.cinema_booking_system.rooms.dto.CinemaRoomResponse;
import com.dare.cinema_booking_system.rooms.service.CinemaRoomService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rooms")
@AllArgsConstructor
public class CinemaRoomController {
	private CinemaRoomService cinemaRoomService;

	@PostMapping
	public ResponseEntity<CinemaRoomResponse> createRoom(@RequestBody @Valid CinemaRoomRequest cinemaRoomRequest){

		return ResponseEntity.ok().body(cinemaRoomService.createCinemaRoom(cinemaRoomRequest));
	}

	@PatchMapping("/update/{id}")
	public ResponseEntity<CinemaRoomResponse> updateRoom(@PathVariable Long id, @RequestBody @Valid CinemaRoomRequest cinemaRoomRequest){
		return ResponseEntity.ok().body(cinemaRoomService.updateCinemaRoom(cinemaRoomRequest, id));
	}

	@GetMapping
	public ResponseEntity<Page<CinemaRoomResponse>> getPageOfRooms(@PageableDefault(page = 0, size = 10,
			sort = "roomNumber", direction = Sort.Direction.ASC) Pageable pageable){
		return ResponseEntity.ok().body(cinemaRoomService.getPageOfCinemaRooms(pageable));
	}
}
