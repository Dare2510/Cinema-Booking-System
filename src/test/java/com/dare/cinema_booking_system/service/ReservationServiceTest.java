package com.dare.cinema_booking_system.service;

import com.dare.cinema_booking_system.movie.dto.MovieResponse;
import com.dare.cinema_booking_system.movie.entity.Genre;
import com.dare.cinema_booking_system.movie.entity.MovieEntity;
import com.dare.cinema_booking_system.movie.service.MovieService;
import com.dare.cinema_booking_system.reservations.dto.ReservationRequest;
import com.dare.cinema_booking_system.reservations.dto.ReservationResponse;
import com.dare.cinema_booking_system.reservations.entity.PaymentEntity;
import com.dare.cinema_booking_system.reservations.entity.PaymentMethod;
import com.dare.cinema_booking_system.reservations.entity.ReservationEntity;
import com.dare.cinema_booking_system.reservations.entity.TicketEntity;
import com.dare.cinema_booking_system.reservations.repository.PaymentRepository;
import com.dare.cinema_booking_system.reservations.repository.ReservationsRepository;
import com.dare.cinema_booking_system.reservations.repository.TicketRepository;
import com.dare.cinema_booking_system.reservations.service.ReservationsService;
import com.dare.cinema_booking_system.rooms.dto.CinemaRoomResponse;
import com.dare.cinema_booking_system.rooms.entity.CinemaRoomEntity;
import com.dare.cinema_booking_system.rooms.entity.SeatEntity;
import com.dare.cinema_booking_system.rooms.service.CinemaRoomService;
import com.dare.cinema_booking_system.screenings.dto.ScreeningRequest;
import com.dare.cinema_booking_system.screenings.dto.ScreeningResponse;
import com.dare.cinema_booking_system.screenings.entity.ScreeningEntity;
import com.dare.cinema_booking_system.screenings.entity.ScreeningSeatEntity;
import com.dare.cinema_booking_system.screenings.entity.ScreeningSeatStatus;
import com.dare.cinema_booking_system.screenings.entity.TimeSlot;
import com.dare.cinema_booking_system.screenings.exceptions.ScreeningSeatNotAvailableException;
import com.dare.cinema_booking_system.screenings.repository.ScreeningRepository;
import com.dare.cinema_booking_system.screenings.repository.ScreeningSeatRepository;
import com.dare.cinema_booking_system.screenings.service.ScreeningService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

	@InjectMocks
	private ReservationsService reservationsService;

	@Mock
	private ScreeningService screeningService;

	@Mock
	private MovieService movieService;

	@Mock
	private CinemaRoomService cinemaRoomService;

	@Mock
	private ScreeningRepository screeningRepository;

	@Mock
	private ScreeningSeatRepository screeningSeatRepository;

	@Mock
	private ReservationsRepository reservationsRepository;

	@Mock
	private TicketRepository  ticketRepository;

	@Mock
	private PaymentRepository  paymentRepository;

	@Spy
	private ModelMapper modelMapper;

	@AfterEach
	public void tearDown() {
		screeningRepository.deleteAll();
		screeningSeatRepository.deleteAll();
		reservationsRepository.deleteAll();
		ticketRepository.deleteAll();
		paymentRepository.deleteAll();
	}
	@Test
	void createReservation_whenReservedSeatsAreFree_returnsReservationResponse() {

		MovieEntity movie = new MovieEntity("testTitle", "testDescription", Genre.COMEDY, 10);
		movie.setId(1L);

		CinemaRoomEntity room = new CinemaRoomEntity(1, 10, 20, new ArrayList<>());
		room.setId(1L);

		LocalDate date = LocalDate.now();
		TimeSlot timeSlot = TimeSlot.PRIME;
		BigDecimal price = BigDecimal.valueOf(10);

		ScreeningEntity screeningEntity = new ScreeningEntity(room.getId(), movie, date, timeSlot, price);
		screeningEntity.setId(1L);

		SeatEntity seat1 = new SeatEntity();
		seat1.setId(1L);
		seat1.setRowNumber(1);
		seat1.setSeatNumber(1);

		SeatEntity seat2 = new SeatEntity();
		seat2.setId(2L);
		seat2.setRowNumber(1);
		seat2.setSeatNumber(2);

		ScreeningSeatEntity screeningSeat1 = new ScreeningSeatEntity();
		screeningSeat1.setCinemaSeats(seat1);
		screeningSeat1.setScreeningSeatStatus(ScreeningSeatStatus.FREE);

		ScreeningSeatEntity screeningSeat2 = new ScreeningSeatEntity();
		screeningSeat2.setCinemaSeats(seat2);
		screeningSeat2.setScreeningSeatStatus(ScreeningSeatStatus.FREE);

		List<ScreeningSeatEntity> screeningSeats = List.of(screeningSeat1, screeningSeat2);

		ReservationRequest reservationRequest =
				new ReservationRequest(1L, List.of(1L, 2L), PaymentMethod.ONLINE);

		when(screeningService.getScreeningEntity(1L))
				.thenReturn(screeningEntity);

		when(screeningSeatRepository.areAllCinemaRoomSeatsFree(
				eq(1L),
				eq(reservationRequest.getCinemaRoomSeatIds()),
				eq((long) reservationRequest.getCinemaRoomSeatIds().size())
		)).thenReturn(true);

		when(screeningSeatRepository.getScreeningSeatsByScreening(screeningEntity))
				.thenReturn(screeningSeats);

		when(reservationsRepository.save(any(ReservationEntity.class)))
				.thenAnswer(invocation -> {
					ReservationEntity reservation = invocation.getArgument(0);
					reservation.setId(1L);
					return reservation;
				});

		when(paymentRepository.save(any(PaymentEntity.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		when(ticketRepository.save(any(TicketEntity.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		when(screeningSeatRepository.saveAll(anyList()))
				.thenAnswer(invocation -> invocation.getArgument(0));


		ReservationResponse response = reservationsService.createReservation(reservationRequest);


		assertNotNull(response);
		assertEquals(1L, response.getReservationId());
		assertEquals(date, response.getScreeningDate());
		assertNotNull(response.getTicketNumber());

		assertEquals(2, response.getReservedSeats().size());
		assertTrue(response.getReservedSeats().contains("Row: 1 - Seat: 1"));
		assertTrue(response.getReservedSeats().contains("Row: 1 - Seat: 2"));

		assertNotNull(response.getPaymentResponse());
		assertEquals(BigDecimal.valueOf(20.0), response.getPaymentResponse().getAmount());

		verify(screeningService).getScreeningEntity(1L);

		verify(screeningSeatRepository).areAllCinemaRoomSeatsFree(
				eq(1L),
				eq(reservationRequest.getCinemaRoomSeatIds()),
				eq((long) reservationRequest.getCinemaRoomSeatIds().size())
		);

		verify(reservationsRepository, times(2)).save(any(ReservationEntity.class));
		verify(paymentRepository).save(any(PaymentEntity.class));
		verify(ticketRepository).save(any(TicketEntity.class));
		verify(screeningSeatRepository).saveAll(anyList());

		assertEquals(ScreeningSeatStatus.RESERVED, screeningSeat1.getScreeningSeatStatus());
		assertEquals(ScreeningSeatStatus.RESERVED, screeningSeat2.getScreeningSeatStatus());
	}

	@Test
	public void createReservation_whenSeatsAreAlreadyReserved_throwsScreeningSeatNotAvailableException() {
		MovieEntity movie = new MovieEntity("testTitle", "testDescription", Genre.COMEDY, 10);
		movie.setId(1L);

		CinemaRoomEntity room = new CinemaRoomEntity(1, 10, 20, new ArrayList<>());
		room.setId(1L);

		LocalDate date = LocalDate.now();
		TimeSlot timeSlot = TimeSlot.PRIME;
		BigDecimal price = BigDecimal.valueOf(10);

		ScreeningEntity screeningEntity = new ScreeningEntity(room.getId(), movie, date, timeSlot, price);
		screeningEntity.setId(1L);

		SeatEntity seat1 = new SeatEntity();
		seat1.setId(1L);
		seat1.setRowNumber(1);
		seat1.setSeatNumber(1);

		SeatEntity seat2 = new SeatEntity();
		seat2.setId(2L);
		seat2.setRowNumber(1);
		seat2.setSeatNumber(2);

		ScreeningSeatEntity screeningSeat1 = new ScreeningSeatEntity();
		screeningSeat1.setCinemaSeats(seat1);
		screeningSeat1.setScreeningSeatStatus(ScreeningSeatStatus.RESERVED);

		ScreeningSeatEntity screeningSeat2 = new ScreeningSeatEntity();
		screeningSeat2.setCinemaSeats(seat2);
		screeningSeat2.setScreeningSeatStatus(ScreeningSeatStatus.RESERVED);

		List<ScreeningSeatEntity> screeningSeats = List.of(screeningSeat1, screeningSeat2);

		ReservationRequest reservationRequest =
				new ReservationRequest(1L, List.of(1L, 2L), PaymentMethod.ONLINE);

		when(screeningService.getScreeningEntity(1L))
				.thenReturn(screeningEntity);

		when(screeningSeatRepository.areAllCinemaRoomSeatsFree(
				eq(1L),
				eq(reservationRequest.getCinemaRoomSeatIds()),
				eq((long) reservationRequest.getCinemaRoomSeatIds().size())
		)).thenReturn(false);

		assertThatThrownBy(() -> reservationsService.createReservation(reservationRequest))
				.isInstanceOf(ScreeningSeatNotAvailableException.class)
				.hasMessage("Chosen seats are not available");

		verify(screeningService).getScreeningEntity(1L);

		verify(screeningSeatRepository).areAllCinemaRoomSeatsFree(
				eq(1L),
				eq(reservationRequest.getCinemaRoomSeatIds()),
				eq((long) reservationRequest.getCinemaRoomSeatIds().size())
		);

		verify(reservationsRepository, never()).save(any(ReservationEntity.class));
		verify(paymentRepository,never()).save(any(PaymentEntity.class));
		verify(ticketRepository,never()).save(any(TicketEntity.class));
		verify(screeningSeatRepository,never()).saveAll(anyList());

	}

}
