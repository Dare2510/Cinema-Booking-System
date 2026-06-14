package com.dare.cinema_booking_system.security.principal;

import com.dare.cinema_booking_system.user.entity.Role;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class AuthenticatedUser {

	private final Long userId;
	private final String email;
	private final Role role;
}
