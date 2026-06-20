package com.dare.cinema_booking_system.user.dto;

import com.dare.cinema_booking_system.user.entity.Role;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserResponse {

	private Long userId;
	private String email;
	private String username;
	private String name;
	private String surname;
	private Role role;
}
