package com.dare.cinema_booking_system.reservations.service;

import com.dare.cinema_booking_system.reservations.dto.ReservationRequest;
import com.dare.cinema_booking_system.reservations.entity.*;
import com.dare.cinema_booking_system.reservations.exceptions.ReservationCompletePaymentException;
import com.dare.cinema_booking_system.reservations.exceptions.ReservationNotFoundException;
import com.dare.cinema_booking_system.reservations.exceptions.ReservationRefundException;
import com.dare.cinema_booking_system.reservations.repository.PaymentRepository;
import com.dare.cinema_booking_system.reservations.repository.ReservationsRepository;
import com.dare.cinema_booking_system.screenings.entity.ScreeningEntity;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@AllArgsConstructor
@Slf4j
@Service
public class PaymentService {

	private PaymentRepository paymentRepository;
	private final ReservationsRepository reservationsRepository;


	@Transactional
	public void completePayment(Long reservationId) {
		ReservationEntity toPay = getReservationById(reservationId);
		PaymentEntity payment = paymentRepository.findByReservation_Id(reservationId);
		PaymentStatus currentPaymentStatus = payment.getPaymentStatus();
		ReservationStatus currentReservationStatus = toPay.getReservationStatus();
		boolean validStatus = currentPaymentStatus.validatorToCompletePayment(currentReservationStatus, currentPaymentStatus);

		if (validStatus) {
			toPay.getPayment().setPaymentStatus(PaymentStatus.PAID);
			toPay.setReservationStatus(ReservationStatus.CONFIRMED);
			reservationsRepository.save(toPay);
			paymentRepository.save(payment);
		} else {
			log.warn("Reservation with id {} has wrong status, payment status {} ,reservation status {}",
					reservationId, currentPaymentStatus, currentReservationStatus);
			throw new ReservationCompletePaymentException(reservationId);
		}
	}

	@Transactional
	public void completeRefund(Long reservationId) {
		ReservationEntity toRefund = getReservationById(reservationId);
		PaymentStatus currentPaymentStatus = toRefund.getPayment().getPaymentStatus();
		ReservationStatus currentReservationStatus = toRefund.getReservationStatus();
		boolean validStatus = currentPaymentStatus.validatorToRefundPayment(currentReservationStatus, currentPaymentStatus);

		if (validStatus) {
			toRefund.getPayment().setPaymentStatus(PaymentStatus.REFUNDED);
			reservationsRepository.save(toRefund);
		} else {
			log.warn("Reservation with id {} has wrong status, payment status {} ,reservation status {}",
					reservationId, currentPaymentStatus, currentReservationStatus);
			throw new ReservationRefundException(reservationId);
		}

	}

	public PaymentEntity createPayment(ReservationRequest reservationRequest, ScreeningEntity screeningToReserve, ReservationEntity newReservation) {
		int numberOfSeats = reservationRequest.getCinemaRoomSeatIds().size();
		BigDecimal pricePerSeat = screeningToReserve.getPrice();
		BigDecimal finalAmount = pricePerSeat.multiply(BigDecimal.valueOf((double) numberOfSeats));
		PaymentMethod paymentMethod = reservationRequest.getPaymentMethod();

		PaymentEntity payments = new PaymentEntity(newReservation, finalAmount, paymentMethod);

		paymentRepository.save(payments);
		log.info("Payment with id {} has been created", payments.getId());

		return payments;

	}

	//Helper Method
	private ReservationEntity getReservationById(Long reservationId) {
		return reservationsRepository.findById(reservationId).orElseThrow(
				() -> {
					log.warn("Could not find reservation with id {}", reservationId);
					return new ReservationNotFoundException(reservationId);
				});
	}

	public PaymentEntity findPaymentByReservationId(Long reservationId) {
		return paymentRepository.findByReservation_Id(reservationId);
	}

	public void statusUpdateCancelPayment(ReservationStatus currentStatus, PaymentEntity paymentToCancel) {
		switch (currentStatus) {
			case CREATED:
				paymentToCancel.setPaymentStatus(PaymentStatus.REFUNDED);
				paymentRepository.save(paymentToCancel);
				break;
			case CONFIRMED:
				paymentToCancel.setPaymentStatus(PaymentStatus.REFUND_PENDING);
				paymentRepository.save(paymentToCancel);
		}

	}
}
