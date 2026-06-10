package com.dare.cinema_booking_system.reservations.controller;

import com.dare.cinema_booking_system.reservations.dto.ReservationRequest;
import com.dare.cinema_booking_system.reservations.dto.ReservationResponse;
import com.dare.cinema_booking_system.reservations.service.PaymentService;
import com.dare.cinema_booking_system.reservations.service.ReservationService;
import com.dare.cinema_booking_system.reservations.service.TicketService;
import jakarta.validation.Valid;
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

	private final ReservationService reservationService;
	private final PaymentService paymentService;
	private final TicketService ticketService;

	@GetMapping
	public ResponseEntity<Page<ReservationResponse>> getPageOfReservations(@PageableDefault(page = 0, size = 10,
			sort = "reservationStatus", direction = Sort.Direction.ASC) Pageable pageable) {
		return ResponseEntity.ok().body(reservationService.getPageOfReservations(pageable));
	}

	@PostMapping
	public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody ReservationRequest reservationRequest) {
		return ResponseEntity.ok(reservationService.createReservation(reservationRequest));
	}

	@GetMapping("/{reservationId}")
	public ResponseEntity<ReservationResponse> getReservationById(@PathVariable Long reservationId) {
		return ResponseEntity.ok(reservationService.findReservationById(reservationId));
	}

	@PatchMapping("/{reservationId}/cancel")
	public ResponseEntity<Void> cancelReservation(@PathVariable Long reservationId) {
		reservationService.cancelReservation(reservationId);
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/{reservationId}/refund")
	public ResponseEntity<Void> refundReservation(@PathVariable Long reservationId) {
		paymentService.completeRefund(reservationId);
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/{reservationId}/complete/payment")
	public ResponseEntity<Void> completePaymentForReservation(@PathVariable Long reservationId) {
		paymentService.completePayment(reservationId);
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/ticket/{ticketNumber}/used")
	public ResponseEntity<Void> setTicketToUsed(@PathVariable String ticketNumber) {
		ticketService.setTicketToUsed(ticketNumber);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/ticket/expire")
	public ResponseEntity<Void> expireTickets() {
		ticketService.setStatusOfExpiredTickets();
		return ResponseEntity.ok().build();
	}


}
