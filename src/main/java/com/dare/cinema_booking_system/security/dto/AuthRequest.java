package com.dare.cinema_booking_system.security.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {

	private String email;
	private String password;
	private String username;
	private String name;
	private String surname;
}
