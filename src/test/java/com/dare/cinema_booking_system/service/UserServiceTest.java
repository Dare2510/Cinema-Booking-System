package com.dare.cinema_booking_system.service;

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
import com.dare.cinema_booking_system.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

	private UserService userService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private ReservationsRepository reservationsRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Spy
	private ModelMapper modelMapper;

	private static final String EMAIL = "testuser@mail.com";
	private static final String PASSWORD = "password";
	private static final String HASHED_PASSWORD = "hashedPassword";
	private static final String USERNAME = "tester";
	private static final String NAME = "testName";
	private static final String SURNAME = "testSurname";

	private static final String UPDATED_EMAIL = "newtestuser@mail.com";
	private static final String UPDATED_USERNAME = "newTester";
	private static final String UPDATED_NAME = "newTestName";
	private static final String UPDATED_SURNAME = "newTestSurname";

	private static final String WRONG_PASSWORD = "Wrong password";

	private static final Role USER_ROLE = Role.USER;
	private static final Role ADMIN_ROLE = Role.ADMIN;
	private static final Long USER_ID = 1L;


	@BeforeEach
	public void setUp() {
		userService = new UserService(userRepository, reservationsRepository, passwordEncoder, modelMapper);
	}

	//Customer Methods Tests

	@Test
	public void registerUserByCustomer_whenEmailIsAvailable_returnUserResponse() {
		UserRequest newUser = userRequestUser();

		when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
		when(passwordEncoder.encode(PASSWORD)).thenReturn(HASHED_PASSWORD);

		when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
			UserEntity user = invocation.getArgument(0);
			user.setId(USER_ID);
			return user;
		});

		UserResponse response = userService.registerUserByCustomer(newUser);

		assertEquals(EMAIL, response.getEmail());
		assertEquals(NAME, response.getName());
		assertEquals(SURNAME, response.getSurname());

		verify(userRepository).findByEmail(EMAIL);
		verify(passwordEncoder).encode(PASSWORD);
		verify(userRepository).save(any(UserEntity.class));
	}

	@Test
	public void registerUserByCustomer_whenEmailIsAlreadyRegistered_throwsUserDoubleCreatedException() {
		UserRequest newUser = userRequestUser();
		UserEntity existingUser = new UserEntity();

		when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));

		assertThatThrownBy(() -> userService.registerUserByCustomer(newUser))
				.isInstanceOf(UserDoubleCreationException.class)
				.hasMessage("User with " + EMAIL + " already exists");

		verify(userRepository).findByEmail(EMAIL);
		verify(passwordEncoder, never()).encode(PASSWORD);
		verify(userRepository, never()).save(any(UserEntity.class));

	}

	@Test
	public void deleteUserByCustomer_deleteIsValid_deletesUser() {
		AuthenticatedUser authenticatedUser = authenticatedUser();
		UserEntity existingUser = new UserEntity();
		existingUser.setPassword(PASSWORD);
		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
		when(reservationsRepository.userCanBeDeleted(existingUser)).thenReturn(true);
		when(passwordEncoder.matches(PASSWORD, existingUser.getPassword())).thenReturn(true);

		userService.deleteUserByCustomer(authenticatedUser, existingUser.getPassword());

		verify(userRepository).delete(existingUser);
		verify(userRepository).findById(USER_ID);
		verify(reservationsRepository).userCanBeDeleted(existingUser);

	}

	@Test
	public void deleteUserByCustomer_deleteNotValid_throwsUserDeletionNotPossibleException() {
		AuthenticatedUser authenticatedUser = authenticatedUser();
		UserEntity existingUser = new UserEntity();
		existingUser.setPassword(PASSWORD);

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
		when(reservationsRepository.userCanBeDeleted(existingUser)).thenReturn(false);
		when(passwordEncoder.matches(PASSWORD, existingUser.getPassword())).thenReturn(true);

		assertThatThrownBy(() -> userService.deleteUserByCustomer(authenticatedUser, PASSWORD))
				.isInstanceOf(UserDeletionNotPossibleException.class)
				.hasMessage("Deletion not possible, you have open reservations");

		verify(userRepository).findById(USER_ID);
		verify(reservationsRepository).userCanBeDeleted(existingUser);
		verify(userRepository, never()).delete(existingUser);

	}

	@Test
	public void updateUserByCustomer_updateIsValid_updatesUser() {
		AuthenticatedUser authenticatedUser = authenticatedUser();
		UserEntity existingUser = new UserEntity();
		UserRequest updatedValues = userRequestUpdatedUser();

		existingUser.setPassword(PASSWORD);
		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
		when(passwordEncoder.matches(PASSWORD, existingUser.getPassword())).thenReturn(true);

		userService.updateUserByCustomer(authenticatedUser, updatedValues, PASSWORD);

		verify(userRepository).save(existingUser);
		verify(userRepository).findById(USER_ID);
		verify(passwordEncoder).matches(PASSWORD, existingUser.getPassword());

		assertEquals(UPDATED_NAME, existingUser.getName());
		assertEquals(UPDATED_USERNAME, existingUser.getUsername());
		assertEquals(UPDATED_EMAIL, existingUser.getEmail());
		assertEquals(UPDATED_SURNAME, existingUser.getSurname());

	}

	@Test
	public void updateUserByCustomer_whenPasswordDoesntMatch_throwsUserIncorrectCredentialsException() {
		AuthenticatedUser authenticatedUser = authenticatedUser();
		UserEntity existingUser = new UserEntity();
		UserRequest updatedValues = userRequestUpdatedUser();

		existingUser.setPassword(PASSWORD);
		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
		when(passwordEncoder.matches(WRONG_PASSWORD, existingUser.getPassword())).thenReturn(false);

		assertThatThrownBy(() -> userService.updateUserByCustomer(authenticatedUser, updatedValues, WRONG_PASSWORD))
				.isInstanceOf(UserIncorrectCredentialsException.class)
				.hasMessage("Invalid password");

		verify(userRepository, never()).save(existingUser);
		verify(userRepository).findById(USER_ID);
		verify(passwordEncoder).matches(WRONG_PASSWORD, existingUser.getPassword());

	}
	//Management Method Tests

	@Test
	public void registerManagement_whenEmailIsValid_registersUser() {
		UserRequest newUser = userRequestUser();

		when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
		when(passwordEncoder.encode(PASSWORD)).thenReturn(PASSWORD);

		when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
			UserEntity user = invocation.getArgument(0);
			user.setId(USER_ID);
			return user;
		});

		UserResponse response = userService.registerManagement(newUser, ADMIN_ROLE);

		assertEquals(EMAIL, response.getEmail());
		assertEquals(NAME, response.getName());
		assertEquals(SURNAME, response.getSurname());

		verify(userRepository).findByEmail(EMAIL);
		verify(passwordEncoder).encode(PASSWORD);
		verify(userRepository).save(any(UserEntity.class));

	}

	@Test
	public void updateUserByManagement_whenUserIsFound_updatesUser() {
		UserEntity existingUser = new UserEntity();
		UserRequest updatedValues = userRequestUpdatedUser();

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
		userService.updateUserByManagement(USER_ID, updatedValues, ADMIN_ROLE);

		assertEquals(ADMIN_ROLE, existingUser.getRole());
		assertEquals(UPDATED_NAME, existingUser.getName());
		assertEquals(UPDATED_USERNAME, existingUser.getUsername());
		assertEquals(UPDATED_EMAIL, existingUser.getEmail());
		assertEquals(UPDATED_SURNAME, existingUser.getSurname());

		verify(userRepository).findById(USER_ID);
		verify(userRepository).save(existingUser);

	}

	@Test
	public void updateUserByManagement_whenUserIsNotFound_throwsUserNotFoundException() {
		UserRequest updatedValues = userRequestUpdatedUser();

		when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.updateUserByManagement(USER_ID, updatedValues, ADMIN_ROLE))
				.isInstanceOf(UserNotFoundException.class)
				.hasMessage("User with id " + USER_ID + " was not found");

		verify(userRepository).findById(USER_ID);
		verify(userRepository, never()).save(any(UserEntity.class));

	}

	//Helper Methods

	private UserRequest userRequestUser() {
		return new UserRequest(EMAIL, PASSWORD, USERNAME, NAME, SURNAME);
	}

	private UserRequest userRequestUpdatedUser() {
		return new UserRequest(UPDATED_EMAIL, PASSWORD, UPDATED_USERNAME, UPDATED_NAME, UPDATED_SURNAME);
	}

	private AuthenticatedUser authenticatedUser() {
		return new AuthenticatedUser(USER_ID, EMAIL, USER_ROLE);
	}


}
