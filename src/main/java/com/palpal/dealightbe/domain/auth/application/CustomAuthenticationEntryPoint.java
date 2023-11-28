package com.palpal.dealightbe.domain.auth.application;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.ErrorResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException authException) throws IOException, ServletException {
		log.warn("권한이 없어 접근이 거부됐습니다: {}", authException.getMessage());

		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.setContentType("application/json;charset=UTF-8");
		String accessDenialResponse = objectMapper.writeValueAsString(
			ErrorResponse.of(ErrorCode.ACCESS_DENIED));
		response.getWriter().write(accessDenialResponse);
		response.getWriter().flush();
		response.getWriter().close();
	}
}
