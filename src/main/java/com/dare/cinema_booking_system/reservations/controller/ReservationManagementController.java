package com.dare.cinema_booking_system.reservations.controller;

import com.dare.cinema_booking_system.reservations.dto.ReservationResponse;
import com.dare.cinema_booking_system.reservations.service.ReservationsService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/management/reservation")
@AllArgsConstructor
public class ReservationManagementController {

	private final ReservationsService reservationsService;

	@GetMapping
	public ResponseEntity<Page<ReservationResponse>> getPageOfReservations(@PageableDefault(page = 0, size = 10,
			sort = "reservationStatus", direction = Sort.Direction.ASC) Pageable pageable) {
		return ResponseEntity.ok().body(reservationsService.getPageOfReservations(pageable));
	}

	@GetMapping("/{reservationId}")
	public ResponseEntity<ReservationResponse> getScreeningById(@PathVariable Long reservationId) {
		return ResponseEntity.ok(reservationsService.findReservationById(reservationId));
	}

	@PatchMapping("/{reservationId}/cancel")
	public ResponseEntity<Void> cancelReservations(@PathVariable Long reservationId) {
		reservationsService.cancelReservation(reservationId);
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/{reservationId}/refund")
	public ResponseEntity<Void> refundReservations(@PathVariable Long reservationId) {
		reservationsService.completeRefund(reservationId);
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/{reservationId}/complete/payment")
	public ResponseEntity<Void> completePaymentReservations(@PathVariable Long reservationId) {
		reservationsService.completePayment(reservationId);
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/ticket/{ticketNumber}/used")
	public ResponseEntity<Void> setTicketToUsed(@PathVariable String ticketNumber) {
		reservationsService.setTicketToUsed(ticketNumber);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/ticket/expire")
	public ResponseEntity<Void> expireTickets() {
		reservationsService.setStatusOfExpiredTickets();
		return ResponseEntity.ok().build();
	}


}
