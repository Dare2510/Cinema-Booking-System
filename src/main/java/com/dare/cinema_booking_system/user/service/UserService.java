package com.dare.cinema_booking_system.user.service;

import com.dare.cinema_booking_system.reservations.repository.ReservationsRepository;
import com.dare.cinema_booking_system.security.principal.AuthenticatedUser;
import com.dare.cinema_booking_system.user.dto.UserRequest;
import com.dare.cinema_booking_system.user.dto.UserResponse;
import com.dare.cinema_booking_system.user.entity.Role;
import com.dare.cinema_booking_system.user.entity.UserEntity;
import com.dare.cinema_booking_system.user.exception.UserDeletionNotPossibleException;
import com.dare.cinema_booking_system.user.exception.UserDoubleCreationException;
import com.dare.cinema_booking_system.user.exception.UserIncorrectCredentialsException;
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
	private final ReservationsRepository reservationsRepository;
	private final PasswordEncoder passwordEncoder;
	private final ModelMapper modelMapper;

	//Customer Methods

	public UserResponse registerUserByCustomer(UserRequest userRequest) {

		if (emailExists(userRequest)) {
			log.info("User with email {} already exists", userRequest.getEmail());
			throw new UserDoubleCreationException(userRequest.getEmail());
		}

		String hashedPassword = passwordEncoder.encode(userRequest.getPassword());

		UserEntity user = new UserEntity();
		updateUserEntity(user,userRequest,hashedPassword);
		user.setRole(Role.USER);

		saveUser(user);

		return responseMapper(user);

	}

	public void deleteUserByCustomer(AuthenticatedUser authenticatedUser,String password) {
		UserEntity toDelete = getUserByAuthenticatedUser(authenticatedUser);
		boolean canBeDeleted = deleteValid(toDelete);

		boolean passwordMatches = passwordEncoder.matches(password, toDelete.getPassword());

		if (!passwordMatches) {
			log.info("Wrong password input for user with id {}", toDelete.getId());
			throw new UserIncorrectCredentialsException();
		}

		if (!canBeDeleted) {
			log.warn("User with email {} and id {} tried to delete his account, failed - open reservations exists",
					toDelete.getEmail(), toDelete.getId());
			throw new UserDeletionNotPossibleException();
		}
		log.info("User with id {} deleted", toDelete.getId());
		userRepository.delete(toDelete);
	}

	public void updateUserByCustomer(AuthenticatedUser authenticatedUser, UserRequest userRequest, String password) {
		UserEntity toUpdate = getUserByAuthenticatedUser(authenticatedUser);

		boolean passwordMatches = passwordEncoder.matches(password, toUpdate.getPassword());

		if (!passwordMatches) {
			log.info("Wrong password input for user with id {}", toUpdate.getId());
			throw new UserIncorrectCredentialsException();
		}
		updateUserEntity(toUpdate, userRequest);

		userRepository.save(toUpdate);
		log.info("User with email {} updated", userRequest.getEmail());
	}

	//Management Methods

	public UserResponse registerManagement(UserRequest userRequest, Role role) {

		if (emailExists(userRequest)) {
			log.info("User with email {} already exists", userRequest.getEmail());
			throw new UserDoubleCreationException(userRequest.getEmail());
		}

		String hashedPassword = passwordEncoder.encode(userRequest.getPassword());

		UserEntity newManagementUser = new UserEntity();
		updateUserEntity(newManagementUser, userRequest, hashedPassword);
		newManagementUser.setRole(role);

		saveUser(newManagementUser);

		return responseMapper(newManagementUser);
	}

	public void updateUserByManagement(Long userId, UserRequest userRequest,Role role) {
		UserEntity toUpdate = getUserById(userId);

		updateUserEntity(toUpdate, userRequest);
		toUpdate.setRole(role);

		userRepository.save(toUpdate);
		log.info("User with email {} updated", toUpdate.getEmail());
	}

	public void deleteUserByManagement(Long userId) {
		UserEntity toDelete = getUserById(userId);
		boolean canBeDeleted = deleteValid(toDelete);

		if (!canBeDeleted) {
			log.warn("User with email {} and id {} has open reservations, deletion not possible",
					toDelete.getEmail(), toDelete.getId());
			throw new UserDeletionNotPossibleException(toDelete.getEmail(),toDelete.getId());
		}
		log.info("User with id {} deleted", toDelete.getId());
		userRepository.delete(toDelete);


	}

	//Helper Methods

	private void updateUserEntity(UserEntity user,UserRequest userRequest, String hashedPassword) {
		user.setEmail(userRequest.getEmail());
		user.setName(userRequest.getName());
		user.setSurname(userRequest.getSurname());
		user.setUsername(userRequest.getUsername());
		user.setEmail(userRequest.getEmail());
		user.setPassword(hashedPassword);
	}

	private void updateUserEntity(UserEntity user,UserRequest userRequest) {
		user.setEmail(userRequest.getEmail());
		user.setName(userRequest.getName());
		user.setSurname(userRequest.getSurname());
		user.setUsername(userRequest.getUsername());
		user.setEmail(userRequest.getEmail());;
	}

	private boolean emailExists(UserRequest userRequest) {
		return userRepository.findByEmail(userRequest.getEmail()).isPresent();
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

	public UserEntity getUserById(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(
						() -> {
							log.info("User with id {} was not found", userId);
							return new UserNotFoundException(userId);
						}
				);
	}

	private void saveUser(UserEntity user) {
		userRepository.save(user);
		log.info("User with role {} and id {} has been registered successfully", user.getRole(), user.getId());
	}

	private boolean deleteValid(UserEntity user) {
		return reservationsRepository.userCanBeDeleted(user);
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
