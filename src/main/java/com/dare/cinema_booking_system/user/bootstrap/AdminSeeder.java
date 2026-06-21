package com.dare.cinema_booking_system.user.bootstrap;

import com.dare.cinema_booking_system.user.entity.Role;
import com.dare.cinema_booking_system.user.entity.UserEntity;
import com.dare.cinema_booking_system.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${app.admin.email}")
	private String adminEmail;

	@Value("${app.admin.password}")
	private String adminPassword;

	@Override
	public void run(String... args) {
		boolean adminExists = userRepository.existsByRole(Role.ADMIN);

		if (adminExists) {
			return;
		}

		UserEntity admin = new UserEntity();
		admin.setEmail(adminEmail);
		admin.setUsername("admin");
		admin.setName("System");
		admin.setSurname("Admin");
		admin.setPassword(passwordEncoder.encode(adminPassword));
		admin.setRole(Role.ADMIN);
		log.info("Boot up Admin created");

		userRepository.save(admin);
	}
}