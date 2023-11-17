package com.palpal.dealightbe.domain.auth.application;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.ErrorResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomAuthAccessDeniedHandler implements AccessDeniedHandler {

	private final ObjectMapper objectMapper;

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
		AccessDeniedException accessDeniedException) throws IOException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Object principal = authentication != null ? authentication.getPrincipal() : null;

		log.warn("권한이 없어 {}의 접근이 거부됐습니다.", principal, accessDeniedException);

		ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.ACCESS_DENIED);
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.setContentType("application/json;charset=UTF-8");
		String accessDenialResponse = objectMapper.writeValueAsString(errorResponse);
		response.getWriter().write(accessDenialResponse);
		response.getWriter().flush();
		response.getWriter().close();
	}
}
