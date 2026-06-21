package com.dare.cinema_booking_system.service;

import com.dare.cinema_booking_system.security.auth.service.AuthService;
import com.dare.cinema_booking_system.security.dto.LoginRequest;
import com.dare.cinema_booking_system.security.jwt.JwtUtil;
import com.dare.cinema_booking_system.user.entity.UserEntity;
import com.dare.cinema_booking_system.user.exception.UserIncorrectCredentialsException;
import com.dare.cinema_booking_system.user.exception.UserNotFoundException;
import com.dare.cinema_booking_system.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

	@Mock
	private UserService userService;

	@Mock
	private JwtUtil jwtUtil;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private AuthService authService;

	@BeforeEach
	public void setUp() {
		authService = new AuthService(passwordEncoder, jwtUtil, userService);
	}

	@Test
	public void login_successful() {
		UserEntity user = existingUser();
		LoginRequest loginRequest = successfulLoginRequest();

		when(userService.getUserEntityByMail(loginRequest.getEmail())).thenReturn(user);
		when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);

		authService.login(loginRequest.getEmail(), loginRequest.getPassword());

		verify(userService).getUserEntityByMail(loginRequest.getEmail());
		verify(jwtUtil).generateToken(user.getEmail(), user.getRole(), user.getId());
		verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPassword());

	}

	@Test
	public void login_failedWithWrongPassword_throwUserIncorrectCredentialsException() {
		UserEntity user = existingUser();
		LoginRequest loginRequest = wrongPasswordLoginRequest();

		when(userService.getUserEntityByMail(loginRequest.getEmail())).thenReturn(user);
		when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(false);

		assertThatThrownBy(() ->
				authService.login(loginRequest.getEmail(), loginRequest.getPassword()))
				.isInstanceOf(UserIncorrectCredentialsException.class)
				.hasMessage("Invalid password");

		verify(userService).getUserEntityByMail(loginRequest.getEmail());
		verify(jwtUtil, never()).generateToken(user.getEmail(), user.getRole(), user.getId());
		verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPassword());

	}

	@Test
	void login_failedWithWrongEmail_throwsUserNotFoundException() {
		LoginRequest loginRequest = wrongEmailLoginRequest();

		when(userService.getUserEntityByMail(loginRequest.getEmail()))
				.thenThrow(new UserNotFoundException(loginRequest.getEmail()));

		assertThrows(UserNotFoundException.class,
				() -> authService.login(loginRequest.getEmail(), loginRequest.getPassword()));

		verify(userService).getUserEntityByMail(loginRequest.getEmail());
	}


	//Helper Methods

	private LoginRequest successfulLoginRequest() {
		return new LoginRequest("testUser@mail.com", "password");
	}

	private LoginRequest wrongPasswordLoginRequest() {
		return new LoginRequest("testUser@mail.com", "wrongPassword");
	}

	private LoginRequest wrongEmailLoginRequest() {
		return new LoginRequest("wrongemail@mail.com", "password");
	}

	private UserEntity existingUser() {
		UserEntity user = new UserEntity();
		user.setPassword("password");
		user.setEmail("testUser@mail.com");
		return user;


	}
}
