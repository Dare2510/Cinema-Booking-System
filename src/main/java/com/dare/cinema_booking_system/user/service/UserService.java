package com.dare.cinema_booking_system.user.service;

import com.dare.cinema_booking_system.security.principal.AuthenticatedUser;
import com.dare.cinema_booking_system.user.dto.UserRequest;
import com.dare.cinema_booking_system.user.dto.UserResponse;
import com.dare.cinema_booking_system.user.entity.Role;
import com.dare.cinema_booking_system.user.entity.UserEntity;
import com.dare.cinema_booking_system.user.exception.UserDoubleCreationException;
import com.dare.cinema_booking_system.user.exception.UserNotFoundException;
import com.dare.cinema_booking_system.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final ModelMapper modelMapper;

	public UserResponse registerUser(UserRequest userRequest) {
		emailExists(userRequest);

		String hashedPassword = passwordEncoder.encode(userRequest.getPassword());

		UserEntity user = createUser(userRequest, hashedPassword);
		user.setRole(Role.USER);

		saveUser(user);

		return responseMapper(user);

	}

	public UserResponse registerManagement(UserRequest userRequest, Role role) {
		emailExists(userRequest);

		String hashedPassword = passwordEncoder.encode(userRequest.getPassword());

		UserEntity user = createUser(userRequest, hashedPassword);
		user.setRole(role);

		saveUser(user);

		return responseMapper(user);
	}

	private UserEntity createUser(UserRequest userRequest, String hashedPassword) {
		UserEntity user = new UserEntity();
		user.setEmail(userRequest.getEmail());
		user.setName(userRequest.getName());
		user.setSurname(userRequest.getSurname());
		user.setUsername(userRequest.getUsername());
		user.setEmail(userRequest.getEmail());
		user.setPassword(hashedPassword);

		return user;
	}

	private void emailExists(UserRequest userRequest) {
		boolean exists = userRepository.findByEmail(userRequest.getEmail()).isPresent();

		if (exists) {
			log.info("User with email {} already exists", userRequest.getEmail());
			throw new UserDoubleCreationException(userRequest.getEmail());
		}
	}

	public UserEntity getUserEntityByMail(String email) {
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

	private UserResponse responseMapper(UserEntity userEntity) {
		return modelMapper.map(userEntity, UserResponse.class);
	}

	public UserEntity getUserByAuthenticatedUser(AuthenticatedUser authenticatedUser) {
		return userRepository.findById(authenticatedUser.getUserId())
				.orElseThrow(() -> {
					log.warn("Could not find user with id {}", authenticatedUser.getUserId());
					return new UserNotFoundException(authenticatedUser.getUserId());
				});
	}


}
