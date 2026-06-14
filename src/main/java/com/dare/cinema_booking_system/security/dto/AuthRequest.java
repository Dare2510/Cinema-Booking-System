package com.dare.cinema_booking_system.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@RequiredArgsConstructor
public class AuthRequest {

	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email address")
	private String email;

	@NotBlank(message = "Password is required")
	@Size(min = 6, max = 72, message = "Password must be between 6 and 72 characters")
	private String password;

	@NotBlank(message = "Username is required")
	@Pattern(
			regexp = "^[a-zA-Z0-9_]+$",
			message = "Username can only contain letters, numbers and underscores"
	)
	@Size(min = 2, max = 10, message = "Username must be between 2 and 10 characters")
	private String username;

	@NotBlank(message = "Name is required")
	@Pattern(
			regexp = "^[a-zA-Z]+$",
			message = "Name can only contain letters"
	)
	@Size(min = 2, max = 20, message = "Name must be between 2 and 20 characters")
	private String name;

	@NotBlank(message = "Surname is required")
	@Pattern(
			regexp = "^[a-zA-Z]+$",
			message = "Surname can only contain letters"
	)
	@Size(min = 2, max = 20, message = "Surname must be between 2 and 20 characters")
	private String surname;
}
