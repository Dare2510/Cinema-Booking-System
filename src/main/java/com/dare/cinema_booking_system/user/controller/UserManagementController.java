package com.dare.cinema_booking_system.user.controller;

import com.dare.cinema_booking_system.user.dto.UserRequest;
import com.dare.cinema_booking_system.user.entity.Role;
import com.dare.cinema_booking_system.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/management/user")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('STAFF','ADMIN')")
public class UserManagementController {

	private final UserService userService;

	@PostMapping("/register")
	public ResponseEntity<String> registerUser(@RequestBody @Valid UserRequest userRequest) {
		userService.registerUser(userRequest);
		return ResponseEntity.ok().body("Register successful");
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/register/{role}")
	public ResponseEntity<String> registerManagement(@RequestBody @Valid UserRequest userRequest, @PathVariable Role role) {
		userService.registerManagement(userRequest, role);
		return ResponseEntity.ok().body("Register successful");
	}
}
