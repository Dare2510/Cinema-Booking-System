package com.dare.cinema_booking_system.user.service;

import com.dare.cinema_booking_system.reservations.repository.ReservationsRepository;
import com.dare.cinema_booking_system.security.principal.AuthenticatedUser;
import com.dare.cinema_booking_system.user.dto.UserPasswordValidationRequest;
import com.dare.cinema_booking_system.user.dto.UserRequest;
import com.dare.cinema_booking_system.user.dto.UserResponse;
import com.dare.cinema_booking_system.user.entity.Role;
import com.dare.cinema_booking_system.user.entity.UserEntity;
import com.dare.cinema_booking_system.user.exception.*;
import com.dare.cinema_booking_system.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
		//For differentiation between customer and management request
		boolean adminCreation = false;

		if (emailExists(userRequest)) {
			log.info("User with email {} already exists", userRequest.getEmail());
			throw new UserDoubleCreationException(userRequest.getEmail());
		}

		try {
			String hashedPassword = passwordEncoder.encode(userRequest.getPassword());

			UserEntity user = new UserEntity();
			updateUserEntity(user, userRequest, hashedPassword);

			//Customer cannot choose role
			user.setRole(Role.USER);

			saveUser(user);

			return responseMapper(user, adminCreation);
		} catch (DataIntegrityViolationException e) {
			log.info("User with email {} already exists (Race Condition)", userRequest.getEmail());
			throw new UserDoubleCreationException(userRequest.getEmail());
		}
	}

	public void deleteUserByCustomer(AuthenticatedUser authenticatedUser, UserPasswordValidationRequest passwordValidationRequest) {
		UserEntity toDelete = getUserByAuthenticatedUser(authenticatedUser);

		//User can only be deleted if there are no open reservations
		boolean canBeDeleted = deleteValid(toDelete);

		String passwordInput = passwordValidationRequest.getPassword();

		boolean passwordMatches = passwordEncoder.matches(passwordInput, toDelete.getPassword());

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

	@Transactional
	public void updateUserByCustomer(AuthenticatedUser authenticatedUser, UserRequest userRequest) {
		UserEntity toUpdate = getUserByAuthenticatedUser(authenticatedUser);

		String passwordInput = userRequest.getPassword();

		boolean passwordMatches = passwordEncoder.matches(passwordInput, toUpdate.getPassword());

		if (!passwordMatches) {
			log.info("Wrong password input for user with id {}", toUpdate.getId());
			throw new UserIncorrectCredentialsException();
		}

		boolean emailIsUsed = userRepository.existsByEmailAndIdNot(userRequest.getEmail(), toUpdate.getId());

		if(emailIsUsed) {
			log.info("User with email {} already exists", toUpdate.getEmail());
			throw new UserEmailAlreadyInUseException(userRequest.getEmail());
		}
		updateUserEntity(toUpdate, userRequest);

		//Security for Race Condition

		try {
			userRepository.saveAndFlush(toUpdate);
		} catch (DataIntegrityViolationException e) {
			log.info("User with id {} tried to update his email address with already existing mail address ", toUpdate.getId());
			throw new UserEmailAlreadyInUseException(toUpdate.getEmail());
		}

		log.info("User with email {} updated", userRequest.getEmail());
	}

	//Management Methods

	public Page<UserResponse> getPageOfUsers(Pageable pageable) {
		//For differentiation between customer and management request
		boolean adminRequest = true;

		return userRepository.findAll(pageable).map(user -> {
			log.info("Getting Page of Users");
			return responseMapper(user, adminRequest);
		});

	}

	//Only for admin
	public UserResponse registerManagement(UserRequest userRequest, Role role) {
		//For differentiation between customer and management request
		boolean adminCreation = true;

		if (emailExists(userRequest)) {
			log.info("User with email {} already exists", userRequest.getEmail());
			throw new UserDoubleCreationException(userRequest.getEmail());
		}

		try {
			String hashedPassword = passwordEncoder.encode(userRequest.getPassword());

			UserEntity newManagementUser = new UserEntity();
			updateUserEntity(newManagementUser, userRequest, hashedPassword);

			//Admin can freely choose role
			newManagementUser.setRole(role);

			saveUser(newManagementUser);

			return responseMapper(newManagementUser, adminCreation);
		} catch (DataIntegrityViolationException e) {
			log.info("User with email {} already exists", userRequest.getEmail());
			throw new UserDoubleCreationException(userRequest.getEmail());
		}


	}

	@Transactional
	public void updateUserByManagement(Long userId, UserRequest userRequest, Role role) {
		UserEntity toUpdate = getUserById(userId);

		toUpdate.setRole(role);

		boolean emailIsUsed = userRepository.existsByEmailAndIdNot(userRequest.getEmail(), toUpdate.getId());

		if(emailIsUsed) {
			log.info("User with email {} already exists", toUpdate.getEmail());
			throw new UserEmailAlreadyInUseException(userRequest.getEmail());
		}
		updateUserEntity(toUpdate, userRequest);

		try {
			userRepository.saveAndFlush(toUpdate);

		} catch (DataIntegrityViolationException e) {
			log.info("User with id {} tried to update his email address with already existing mail address ", toUpdate.getId());
			throw new UserEmailAlreadyInUseException(userRequest.getEmail());
		}
		log.info("User with email {} updated", toUpdate.getEmail());
	}

	public void deleteUserByManagement(Long userId) {
		UserEntity toDelete = getUserById(userId);

		//User can only be deleted if there are no open reservations
		boolean canBeDeleted = deleteValid(toDelete);

		if (!canBeDeleted) {
			log.warn("User with email {} and id {} has open reservations, deletion not possible",
					toDelete.getEmail(), toDelete.getId());
			throw new UserDeletionNotPossibleException(toDelete.getEmail(), toDelete.getId());
		}
		log.info("User with id {} deleted", toDelete.getId());
		userRepository.delete(toDelete);


	}

	//Helper Methods

	private void updateUserEntity(UserEntity user, UserRequest userRequest, String hashedPassword) {
		user.setEmail(userRequest.getEmail());
		user.setName(userRequest.getName());
		user.setSurname(userRequest.getSurname());
		user.setUsername(userRequest.getUsername());
		user.setEmail(userRequest.getEmail());
		user.setPassword(hashedPassword);
	}

	private void updateUserEntity(UserEntity user, UserRequest userRequest) {
		user.setEmail(userRequest.getEmail());
		user.setName(userRequest.getName());
		user.setSurname(userRequest.getSurname());
		user.setUsername(userRequest.getUsername());
		user.setEmail(userRequest.getEmail());
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
		userRepository.saveAndFlush(user);
		log.info("User with role {} and id {} has been registered successfully", user.getRole(), user.getId());
	}

	private boolean deleteValid(UserEntity user) {
		return reservationsRepository.userCanBeDeleted(user);
	}

	private UserResponse responseMapper(UserEntity userEntity, boolean adminCreation) {
		if (adminCreation) {
			return modelMapper.map(userEntity, UserResponse.class);
		} else {
			return UserResponse.builder()
					.userId(userEntity.getId())
					.email(userEntity.getEmail())
					.name(userEntity.getName())
					.surname(userEntity.getSurname())
					.username(userEntity.getUsername())
					.build();
		}
	}

	public UserEntity getUserByAuthenticatedUser(AuthenticatedUser authenticatedUser) {
		return userRepository.findById(authenticatedUser.getUserId())
				.orElseThrow(() -> {
					log.warn("Could not find user with id {}", authenticatedUser.getUserId());
					return new UserNotFoundException(authenticatedUser.getUserId());
				});
	}

}
