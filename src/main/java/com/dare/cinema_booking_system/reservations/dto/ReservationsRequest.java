package com.dare.cinema_booking_system.reservations.dto;

import com.dare.cinema_booking_system.reservations.entity.PaymentMethod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class ReservationsRequest {

	@NotNull
	@Min(value = 1, message = "Screening id must be at least 1")
	public Long screeningId;

	@NotNull
	@Min(value = 1, message = "Screening seat id must be at least 1")
	@Size(min = 1, message = "At least 1 seat id is required")
	public List<Long> screeningSeatId;

	@NotNull(message = "Payment method is required, available ONLINE, ON_SITE")
	public PaymentMethod paymentMethod;

}
