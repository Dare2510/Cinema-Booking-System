package com.dare.cinema_booking_system.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserResponse {

	private Long userId;
	private String email;
	private String username;
	private String name;
	private String surname;
}
