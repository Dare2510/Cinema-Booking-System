package com.dare.cinema_booking_system.reservations.controller;

import com.dare.cinema_booking_system.reservations.dto.ReservationsRequest;
import com.dare.cinema_booking_system.reservations.dto.ReservationsResponse;
import com.dare.cinema_booking_system.reservations.service.ReservationsService;
import com.dare.cinema_booking_system.rooms.dto.CinemaRoomResponse;
import com.dare.cinema_booking_system.screenings.dto.ScreeningSeatResponse;

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
@RequestMapping("/api/reservation/")
@AllArgsConstructor
public class ReservationsController {

	private final ReservationsService reservationsService;

	@GetMapping("seats/{screeningId}")
	public ResponseEntity<List<ScreeningSeatResponse>> getFreeSeats(@PathVariable Long screeningId) {
		return ResponseEntity.ok(reservationsService.getFreeScreeningSeatsByScreeningId(screeningId));
	}

	@GetMapping("/{reservationId}")
	public ResponseEntity<ReservationsResponse> getScreeningById(@PathVariable Long reservationId) {
		return ResponseEntity.ok(reservationsService.findReservationById(reservationId));
	}
	@PostMapping
	public ResponseEntity<ReservationsResponse> createReservations (@Valid @RequestBody ReservationsRequest reservationsRequest) {
		return ResponseEntity.ok(reservationsService.createReservation(reservationsRequest));
	}
	@PatchMapping("/cancel/{reservationId}")
	public ResponseEntity<Void> cancelReservations(@PathVariable Long reservationId) {
		reservationsService.cancelReservation(reservationId);
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/refund/{reservationId}")
	public ResponseEntity<Void> refundReservations(@PathVariable Long reservationId) {
		reservationsService.completeRefund(reservationId);
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/payment/{reservationId}")
	public ResponseEntity<Void> completePaymentReservations(@PathVariable Long reservationId) {
		reservationsService.completePayment(reservationId);
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/use/{ticketNumber}")
	public ResponseEntity<Void> useTicket(@PathVariable String ticketNumber) {
		reservationsService.setTicketToUsed(ticketNumber);
		return ResponseEntity.ok().build();
	}

	@GetMapping
	public ResponseEntity<Page<ReservationsResponse>> getPageOfRooms(@PageableDefault(page = 0, size = 10,
			sort = "reservationStatus", direction = Sort.Direction.ASC) Pageable pageable) {
		return ResponseEntity.ok().body(reservationsService.getPageOfReservations(pageable));
	}
	@PatchMapping("/expired")
	public ResponseEntity<Void> expireTickets(){
		reservationsService.setStatusOfExpiredTickets();
		return ResponseEntity.ok().build();
	}
}
