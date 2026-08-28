package com.dare.cinema_booking_system.integration;

import com.dare.cinema_booking_system.security.principal.AuthenticatedUser;
import com.dare.cinema_booking_system.user.dto.UserRequest;
import com.dare.cinema_booking_system.user.dto.UserResponse;
import com.dare.cinema_booking_system.user.entity.Role;
import com.dare.cinema_booking_system.user.exception.UserDoubleCreationException;
import com.dare.cinema_booking_system.user.exception.UserEmailAlreadyInUseException;
import com.dare.cinema_booking_system.user.repository.UserRepository;
import com.dare.cinema_booking_system.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.*;
import java.util.stream.Stream;

@SpringBootTest
@ActiveProfiles("test-postgres")
@Testcontainers
public class UserConcurrencyIntegrationTest {

	@Container
	@ServiceConnection
	public static PostgreSQLContainer postgreSQLContainer =
				new PostgreSQLContainer("postgres:17-alpine");

	private static final String EMAIL = "testuser@mail.com";
	private static final String PASSWORD = "password";
	private static final String USERNAME = "tester";
	private static final String NAME = "testName";
	private static final String SURNAME = "testSurname";

	private static final String UPDATED_EMAIL = "updatedTestuser@mail.com";


	@Autowired
	private UserService userService;
	@Autowired
	private UserRepository userRepository;

	@Test
	public void concurrentUserCreation_whenEmailIsTheSame_throwsUserAlreadyExists() throws Exception{
		UserRequest userRequest = new UserRequest(EMAIL,PASSWORD,USERNAME,NAME,SURNAME);

		ExecutorService executors = Executors.newFixedThreadPool(2);

		CountDownLatch ready = new CountDownLatch(2);

		CountDownLatch start =  new CountDownLatch(1);

		Callable<Boolean> task = () -> {

			try {

				ready.countDown();

				start.await();

				userService.registerUserByCustomer(userRequest);

				return true;
			} catch (UserDoubleCreationException e) {

				return false;
			}
		};

		Future<Boolean> registerA = executors.submit(task);
		Future<Boolean> registerB = executors.submit(task);

		ready.await();
		start.countDown();

		boolean successA = registerA.get();
		boolean successB = registerB.get();

		executors.shutdown();

		long successSum =
				Stream.of(successA, successB)
						.filter(Boolean::booleanValue)
						.count();

		assertEquals(1, successSum);
		assertEquals(1,userRepository.countByEmail(EMAIL));
	}

	@Test
	public void concurrentUserCreationAndUpdate_whenEmailIsUsed_throwsUserEmailAlreadyInUseExceptionORUserDoubleCreationException() throws Exception {
		UserRequest requestForRegisterThread = new UserRequest(UPDATED_EMAIL,PASSWORD,USERNAME,NAME,SURNAME);

		UserRequest requestForUpdateThreadCreateUser = new UserRequest(EMAIL,PASSWORD,USERNAME,NAME,SURNAME);
		UserRequest requestForUpdateThreadToUpdateUser = new UserRequest(UPDATED_EMAIL,PASSWORD,USERNAME,NAME,SURNAME);

		UserResponse registerResponseForUpdate = userService.registerUserByCustomer(requestForUpdateThreadCreateUser);


		ExecutorService executors = Executors.newFixedThreadPool(2);

		CountDownLatch ready = new CountDownLatch(2);

		CountDownLatch start =  new CountDownLatch(1);

		Callable<Boolean> register = () -> {
			try {
				ready.countDown();

				start.await();

				userService.registerUserByCustomer(requestForRegisterThread);

				return true;
			} catch (UserDoubleCreationException e) {
				return false;
			}
		};



		Callable<Boolean> update = () -> {
			try {
				ready.countDown();

				start.await();

				userService.updateUserByCustomer(authenticatedUser(registerResponseForUpdate.getUserId()), requestForUpdateThreadToUpdateUser);

				return true;
			} catch (UserEmailAlreadyInUseException e) {
				return false;
			}
		};

		Future<Boolean> userRegistered = executors.submit(register);
		Future<Boolean> userUpdated = executors.submit(update);

		ready.await();

		start.countDown();

		boolean registerSuccess = userRegistered.get();
		boolean updateSuccess = userUpdated.get();

		executors.shutdown();

		long successSum =
				Stream.of(registerSuccess, updateSuccess)
						.filter(Boolean::booleanValue)
						.count();

		assertEquals(1, successSum);
		assertEquals(1,userRepository.countByEmail(UPDATED_EMAIL));

	}

	private AuthenticatedUser authenticatedUser(Long userId) {
		return new AuthenticatedUser(userId, UPDATED_EMAIL, Role.USER);
	}


}
