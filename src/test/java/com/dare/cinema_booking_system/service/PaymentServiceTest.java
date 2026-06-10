package com.dare.cinema_booking_system.service;

import com.dare.cinema_booking_system.movie.entity.Genre;
import com.dare.cinema_booking_system.movie.entity.MovieEntity;
import com.dare.cinema_booking_system.reservations.dto.ReservationRequest;
import com.dare.cinema_booking_system.reservations.entity.*;
import com.dare.cinema_booking_system.reservations.exceptions.ReservationCompletePaymentException;
import com.dare.cinema_booking_system.reservations.exceptions.ReservationRefundException;
import com.dare.cinema_booking_system.reservations.repository.PaymentRepository;
import com.dare.cinema_booking_system.reservations.repository.ReservationsRepository;
import com.dare.cinema_booking_system.reservations.service.PaymentService;
import com.dare.cinema_booking_system.screenings.entity.ScreeningEntity;
import com.dare.cinema_booking_system.screenings.entity.TimeSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static final Long RESERVATION_ID = 1L;
    private static final Long PAYMENT_ID = 1L;
    private static final Long SCREENING_ID = 1L;

    private PaymentService paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReservationsRepository reservationsRepository;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, reservationsRepository);
    }

    @Test
    void completePayment_whenStatusIsValid_completesPaymentAndConfirmsReservation() {
        ReservationEntity reservation = reservation(ReservationStatus.CREATED, PaymentStatus.UNPAID);

        when(reservationsRepository.findById(RESERVATION_ID)).thenReturn(Optional.of(reservation));
        when(paymentRepository.findByReservation_Id(RESERVATION_ID)).thenReturn(reservation.getPayment());

        paymentService.completePayment(RESERVATION_ID);

        assertEquals(ReservationStatus.CONFIRMED, reservation.getReservationStatus());
        assertEquals(PaymentStatus.PAID, reservation.getPayment().getPaymentStatus());
        verify(reservationsRepository).save(reservation);
        verify(paymentRepository).save(reservation.getPayment());
    }

    @Test
    void completePayment_whenStatusIsInvalid_throwsReservationCompletePaymentException() {
        ReservationEntity reservation = reservation(ReservationStatus.CANCELLED, PaymentStatus.REFUNDED);

        when(reservationsRepository.findById(RESERVATION_ID)).thenReturn(Optional.of(reservation));
        when(paymentRepository.findByReservation_Id(RESERVATION_ID)).thenReturn(reservation.getPayment());

        assertThatThrownBy(() -> paymentService.completePayment(RESERVATION_ID))
                .isInstanceOf(ReservationCompletePaymentException.class)
                .hasMessage("Payment for reservation with " + RESERVATION_ID + " id cannot be completed." +
                        " Check status of reservation and payment");

        assertEquals(ReservationStatus.CANCELLED, reservation.getReservationStatus());
        assertEquals(PaymentStatus.REFUNDED, reservation.getPayment().getPaymentStatus());
        verify(reservationsRepository, never()).save(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void completeRefund_whenStatusIsValid_refundsPayment() {
        ReservationEntity reservation = reservation(ReservationStatus.CANCELLED, PaymentStatus.REFUND_PENDING);

        when(reservationsRepository.findById(RESERVATION_ID)).thenReturn(Optional.of(reservation));

        paymentService.completeRefund(RESERVATION_ID);

        assertEquals(PaymentStatus.REFUNDED, reservation.getPayment().getPaymentStatus());
        verify(reservationsRepository).save(reservation);
    }

    @Test
    void completeRefund_whenStatusIsInvalid_throwsReservationRefundException() {
        ReservationEntity reservation = reservation(ReservationStatus.CREATED, PaymentStatus.UNPAID);

        when(reservationsRepository.findById(RESERVATION_ID)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> paymentService.completeRefund(RESERVATION_ID))
                .isInstanceOf(ReservationRefundException.class);

        verify(reservationsRepository, never()).save(any());
    }

    @Test
    void createPayment_createsPaymentWithCorrectAmountAndMethod() {
        ReservationEntity reservation = new ReservationEntity();
        ReservationRequest request = new ReservationRequest(SCREENING_ID, List.of(1L, 2L), PaymentMethod.ONLINE);
        ScreeningEntity screening = screening(BigDecimal.valueOf(10));

        when(paymentRepository.save(any(PaymentEntity.class)))
                .thenAnswer(invocation -> {
                    PaymentEntity payment = invocation.getArgument(0);
                    payment.setId(PAYMENT_ID);
                    return payment;
                });

        PaymentEntity payment = paymentService.createPayment(request, screening, reservation);

        assertNotNull(payment);
        assertEquals(PAYMENT_ID, payment.getId());
        assertSame(reservation, payment.getReservation());
        assertEquals(0, BigDecimal.valueOf(20).compareTo(payment.getAmount()));
        assertEquals(PaymentMethod.ONLINE, payment.getPaymentMethod());
        assertEquals(PaymentStatus.UNPAID, payment.getPaymentStatus());
        verify(paymentRepository).save(payment);
    }

    @Test
    void statusUpdateCancelPayment_whenReservationWasCreated_setsPaymentToRefunded() {
        ReservationEntity reservation = reservation(ReservationStatus.CREATED, PaymentStatus.UNPAID);

        paymentService.statusUpdateCancelPayment(ReservationStatus.CREATED, reservation.getPayment());

        assertEquals(PaymentStatus.REFUNDED, reservation.getPayment().getPaymentStatus());
        verify(paymentRepository).save(reservation.getPayment());
    }

    @Test
    void statusUpdateCancelPayment_whenReservationWasConfirmed_setsPaymentToRefundPending() {
        ReservationEntity reservation = reservation(ReservationStatus.CONFIRMED, PaymentStatus.PAID);

        paymentService.statusUpdateCancelPayment(ReservationStatus.CONFIRMED, reservation.getPayment());

        assertEquals(PaymentStatus.REFUND_PENDING, reservation.getPayment().getPaymentStatus());
        verify(paymentRepository).save(reservation.getPayment());
    }

    private ReservationEntity reservation(ReservationStatus reservationStatus, PaymentStatus paymentStatus) {
        ReservationEntity reservation = new ReservationEntity();
        reservation.setId(RESERVATION_ID);
        reservation.setReservationStatus(reservationStatus);

        PaymentEntity payment = new PaymentEntity(reservation, BigDecimal.valueOf(20), PaymentMethod.ONLINE);
        payment.setId(PAYMENT_ID);
        payment.setPaymentStatus(paymentStatus);
        reservation.setPayment(payment);
        return reservation;
    }

    private ScreeningEntity screening(BigDecimal price) {
        MovieEntity movie = new MovieEntity("title", "description", Genre.COMEDY, 90);
        movie.setId(1L);
        ScreeningEntity screening = new ScreeningEntity(1L, movie, LocalDate.now(), TimeSlot.PRIME, price);
        screening.setId(SCREENING_ID);
        return screening;
    }
}
