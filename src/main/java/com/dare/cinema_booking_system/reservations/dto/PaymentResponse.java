package com.dare.cinema_booking_system.reservations.dto;

import com.dare.cinema_booking_system.reservations.entity.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponse {

	private PaymentStatus paymentStatus;
	private String iban;
	private String bankName;
	String paymentReference;
	private BigDecimal amount;
}
