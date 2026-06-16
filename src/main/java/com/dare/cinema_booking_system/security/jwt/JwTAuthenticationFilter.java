package com.dare.cinema_booking_system.security.jwt;

import com.dare.cinema_booking_system.security.principal.AuthenticatedUser;
import com.dare.cinema_booking_system.user.entity.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwTAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {

		String authorizationHeader = request.getHeader("Authorization");

		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			String token = authorizationHeader.substring(7);

			String email = jwtUtil.extractEmail(token);
			Role role = jwtUtil.extractRole(token);
			Long userId = jwtUtil.extractUserId(token);

			AuthenticatedUser principal = new AuthenticatedUser(userId, email, role);

			UsernamePasswordAuthenticationToken authentication =
					new UsernamePasswordAuthenticationToken(
							principal,
							null,
							List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
					);

			SecurityContextHolder.getContext().setAuthentication(authentication);

		} catch (Exception e) {
			SecurityContextHolder.clearContext();
			throw new BadCredentialsException("Bad or Invalid JWT Token", e);
		}

		filterChain.doFilter(request, response);
	}
}
