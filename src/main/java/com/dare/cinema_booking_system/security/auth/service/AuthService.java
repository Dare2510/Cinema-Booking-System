package com.dare.cinema_booking_system.security.auth.service;

import com.dare.cinema_booking_system.security.jwt.JwtUtil;
import com.dare.cinema_booking_system.user.entity.UserEntity;
import com.dare.cinema_booking_system.user.exception.UserIncorrectCredentialsException;
import com.dare.cinema_booking_system.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;
	private final UserService userService;

	public String login(String email, String password) {
		UserEntity user = userService.getUserEntityByMail(email);

		boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());

		if (!passwordMatches) {
			log.info("Wrong password input for user with id {}", user.getId());
			throw new UserIncorrectCredentialsException();
		}
		log.info("User with id {} logged in successfully", user.getId());
		return jwtUtil.generateToken(email, user.getRole(), user.getId());
	}


}
