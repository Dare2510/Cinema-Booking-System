package com.dare.cinema_booking_system.security.auth.service;

import com.dare.cinema_booking_system.security.dto.AuthRequest;
import com.dare.cinema_booking_system.user.entity.Role;
import com.dare.cinema_booking_system.user.entity.UserEntity;
import com.dare.cinema_booking_system.user.exception.UserDoubleCreationException;
import com.dare.cinema_booking_system.user.exception.UserNotFoundException;
import com.dare.cinema_booking_system.security.jwt.JwtUtil;
import com.dare.cinema_booking_system.user.repository.UserRepository;
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
		UserEntity user = getUserEntity(email);

		boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());

		if (!passwordMatches) {
			log.info("Wrong password input for user with id {}", user.getId());
			throw new RuntimeException("Invalid credentials");
		}

		return jwtUtil.generateToken(email, user.getRole(), user.getId());
	}

	public void registerUser(AuthRequest authRequest) {
		emailExists(authRequest);

		String hashedPassword = passwordEncoder.encode(authRequest.getPassword());

		UserEntity user = createUser(authRequest, hashedPassword);
		user.setRole(Role.USER);

		saveUser(user);

	}

	public void registerManagement(AuthRequest authRequest, Role role) {
		emailExists(authRequest);

		String hashedPassword = passwordEncoder.encode(authRequest.getPassword());

		UserEntity user = createUser(authRequest, hashedPassword);
		user.setRole(role);

		saveUser(user);
	}

	private void emailExists(AuthRequest authRequest) {
		boolean exists = userRepository.findByEmail(authRequest.getEmail()).isPresent();

		if (exists) {
			log.info("User with email {} already exists", authRequest.getEmail());
			throw new UserDoubleCreationException(authRequest.getEmail());
		}
	}

	private UserEntity getUserEntity(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(
						() -> {
							log.info("User with email {} was not found", email);
							return new UserNotFoundException(email);
						}
				);
	}

	private void saveUser(UserEntity user) {
		userRepository.save(user);
		log.info("User with role {} and id {} has been registered successfully", user.getRole(), user.getId());
	}

	private UserEntity createUser(AuthRequest authRequest, String hashedPassword) {
		UserEntity user = new UserEntity();
		user.setEmail(authRequest.getEmail());
		user.setName(authRequest.getName());
		user.setSurname(authRequest.getSurname());
		user.setUsername(authRequest.getUsername());
		user.setEmail(authRequest.getEmail());
		user.setPassword(hashedPassword);

		return user;
	}


}
