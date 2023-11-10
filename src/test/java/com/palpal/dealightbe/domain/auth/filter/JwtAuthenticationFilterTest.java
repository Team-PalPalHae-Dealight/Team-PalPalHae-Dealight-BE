package com.palpal.dealightbe.domain.auth.filter;

import static org.mockito.BDDMockito.*;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.palpal.dealightbe.domain.auth.domain.Jwt;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

	@Mock
	HttpServletRequest mockRequest;
	@Mock
	HttpServletResponse mockResponse;
	@Mock
	FilterChain mockFilterChain;
	@Mock
	Jwt mockJwt;
	@InjectMocks
	JwtAuthenticationFilter jwtAuthenticationFilter;
	@Mock
	Authentication mockAuthentication;
	@Mock
	SecurityContext mockSecurityContext;

	@BeforeEach
	void setUp() {
		jwtAuthenticationFilter = new JwtAuthenticationFilter(mockJwt);
	}

	@AfterEach
	void tearDown() {
		// MockTest 격리를 위한 값
		Mockito.reset(mockRequest, mockResponse, mockFilterChain, mockJwt, mockAuthentication, mockSecurityContext);
	}

	@DisplayName("JWT 인증이 정상적으로 완료된 경우")
	@Test
	void authenticationSuccessTest() throws ServletException, IOException {
		// given
		when(mockRequest.getHeader(HttpHeaders.AUTHORIZATION))
			.thenReturn("Bearer MOCK_JWT_TOKEN");

		// when
		jwtAuthenticationFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

		// then
		verify(mockRequest, times(1))
			.getHeader(any());
		verify(mockJwt, times(1))
			.validateToken(any(String.class));
		verify(mockFilterChain, times(1))
			.doFilter(mockRequest, mockResponse);
	}

	@DisplayName("인증이 이미 완료된 경우")
	@Test
	void alreadyDoneAuthentication() throws ServletException, IOException {
		// given
		when(mockSecurityContext.getAuthentication())
			.thenReturn(mockAuthentication);
		SecurityContextHolder.setContext(mockSecurityContext);

		// when
		jwtAuthenticationFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

		// then
		verify(SecurityContextHolder.getContext(), times(1))
			.getAuthentication();
		verify(mockFilterChain, times(1))
			.doFilter(mockRequest, mockResponse);
	}

	@DisplayName("Authorization에 토큰이 없는 경우")
	@Test
	void tokenIsNotOnAuthorizationHeader() throws ServletException, IOException {
		// given
		when(mockRequest.getHeader(HttpHeaders.AUTHORIZATION))
			.thenReturn(null);

		// when
		jwtAuthenticationFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

		// then
		verify(mockFilterChain, times(1))
			.doFilter(mockRequest, mockResponse);
	}
}
