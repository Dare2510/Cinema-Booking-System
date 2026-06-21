package com.dare.cinema_booking_system.security.principal;

import com.dare.cinema_booking_system.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthenticatedUser {

	private Long userId;
	private String email;
	private Role role;
}
