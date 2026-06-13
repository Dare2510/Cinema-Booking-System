package com.dare.cinema_booking_system.security.service;

import com.dare.cinema_booking_system.security.dto.AuthRequest;
import com.dare.cinema_booking_system.security.entity.Role;
import com.dare.cinema_booking_system.security.entity.UserEntity;
import com.dare.cinema_booking_system.security.jwt.JwtUtil;
import com.dare.cinema_booking_system.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;

	public String login(String email, String password) {

		UserEntity user = userRepository.findByEmail(email)
				.orElseThrow(
						() -> new RuntimeException("Invalid credentials)")
				);

		boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());

		if (!passwordMatches) {
			throw new RuntimeException("Invalid credentials");
		}

		return jwtUtil.generateToken(email, user.getRole());
	}

	public void register(AuthRequest authRequest) {
		if (userRepository.findByEmail(authRequest.getEmail()).isPresent()) {
			throw new RuntimeException("User already exists");
		}
		String hashedPassword = passwordEncoder.encode(authRequest.getPassword());

		UserEntity user = new UserEntity();
		user.setEmail(authRequest.getEmail());
		user.setName(authRequest.getName());
		user.setSurname(authRequest.getSurname());
		user.setUsername(authRequest.getUsername());
		user.setEmail(authRequest.getEmail());
		user.setPassword(hashedPassword);
		user.setRole(Role.USER);

		userRepository.save(user);
		log.info("User registered successfully");
	}

	public void registerManagement(AuthRequest authRequest, Role role) {
		if (userRepository.findByEmail(authRequest.getEmail()).isPresent()) {
			throw new RuntimeException("User already exists");
		}
		String hashedPassword = passwordEncoder.encode(authRequest.getPassword());

		UserEntity user = new UserEntity();
		user.setEmail(authRequest.getEmail());
		user.setName(authRequest.getName());
		user.setSurname(authRequest.getSurname());
		user.setUsername(authRequest.getUsername());
		user.setEmail(authRequest.getEmail());
		user.setPassword(hashedPassword);
		user.setRole(role);

		userRepository.save(user);
	}
}
