package com.dare.cinema_booking_system.user.controller;

import com.dare.cinema_booking_system.user.dto.UserRequest;
import com.dare.cinema_booking_system.user.dto.UserResponse;
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
	public ResponseEntity<UserResponse> registerUser(@RequestBody @Valid UserRequest userRequest) {
		return ResponseEntity.ok().body(userService.registerUserByCustomer(userRequest));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/register/{role}")
	public ResponseEntity<UserResponse> registerManagement(@RequestBody @Valid UserRequest userRequest, @PathVariable Role role) {
		return ResponseEntity.ok().body(userService.registerManagement(userRequest, role));
	}

	@PatchMapping("/{userId}/{role}/update")
	public ResponseEntity<Void> updateUser(@RequestBody @Valid UserRequest userRequest, @PathVariable Long userId,@PathVariable Role role) {
		userService.updateUserByManagement(userId,userRequest,role);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{userId}/delete")
	public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
		userService.deleteUserByManagement(userId);
		return ResponseEntity.ok().build();
	}
}
