package com.dare.cinema_booking_system.security.jwt;

import com.dare.cinema_booking_system.security.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class AuthenticatedUser {

	private final String email;
	private final Role role;
}
