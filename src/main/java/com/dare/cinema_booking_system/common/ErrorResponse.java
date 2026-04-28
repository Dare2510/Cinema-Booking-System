package com.dare.cinema_booking_system.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@AllArgsConstructor
@Getter
@Setter
public class ErrorResponse {

	private int status;
	private String message;
	LocalDateTime timestamp;

}
