package com.dare.cinema_booking_system.integration;

import com.dare.cinema_booking_system.security.dto.LoginRequest;
import com.dare.cinema_booking_system.security.jwt.JwtUtil;
import com.dare.cinema_booking_system.user.dto.UserRequest;
import com.dare.cinema_booking_system.user.entity.Role;
import com.dare.cinema_booking_system.user.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class AuthIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JwtUtil jwtUtil;

	private static final String USER_MAIL = "testuser@mail.com";
	private static final String USER_USERNAME = "testUser";
	private static final String USER_FIRST_NAME = "testUserFirstName";
	private static final String USER_SURNAME = "testUserSurname";
	private static final String PASSWORD = "password";

	@AfterEach
	public void tearDown() {
		userRepository.deleteAll();
	}


	@Test
	public void login_requestIsValid_returnsJWT() throws Exception {
		UserRequest newUser = newUser();
		Long userId = createUserAndGetId(newUser);

		LoginRequest loginRequest = validRequest();
		String token = generateToken(userId);

		String response = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginRequest)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		assertEquals(token.length(), response.length());

	}

	@Test
	public void login_passwordIsInvalid_returnsBadRequest() throws Exception {
		UserRequest newUser = newUser();
		createUser(newUser);

		LoginRequest loginRequest = invalidRequestWrongPassword();

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginRequest)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message")
						.value("Invalid password"));

	}

	@Test
	public void login_emailIsInvalid_returnsNotFound() throws Exception {
		UserRequest newUser = newUser();
		createUser(newUser);

		LoginRequest loginRequest = invalidRequestWrongEmail();

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginRequest)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message")
						.value("User with email: " + loginRequest.getEmail() + " was not found"));
	}

	//Helper Methods

	//Requests

	private UserRequest newUser() {
		return new UserRequest(USER_MAIL, PASSWORD, USER_USERNAME, USER_FIRST_NAME, USER_SURNAME);
	}

	private LoginRequest validRequest() {
		return new LoginRequest(USER_MAIL, PASSWORD);
	}

	private LoginRequest invalidRequestWrongPassword() {
		return new LoginRequest(USER_MAIL, "wrongPassword");
	}

	private LoginRequest invalidRequestWrongEmail() {
		return new LoginRequest("wrongemail@mail.com", PASSWORD);
	}

	//Endpoint Helper

	private Long createUserAndGetId(UserRequest userRequest) throws Exception {
		String response = mockMvc.perform(post("/api/user/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userRequest)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return ((Number) JsonPath.read(response, "$.userId")).longValue();
	}

	private void createUser(UserRequest userRequest) throws Exception {
		mockMvc.perform(post("/api/user/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userRequest)))
				.andExpect(status().isOk());
	}

	//JWT

	private String generateToken(Long userId) {
		return jwtUtil.generateToken(USER_MAIL, Role.USER, userId);
	}


}
