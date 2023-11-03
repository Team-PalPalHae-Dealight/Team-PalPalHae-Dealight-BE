package com.palpal.dealightbe.domain.auth.filter;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palpal.dealightbe.domain.auth.domain.Jwt;
import com.palpal.dealightbe.domain.auth.domain.JwtAuthentication;
import com.palpal.dealightbe.domain.auth.domain.JwtAuthenticationToken;
import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.ErrorResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final Jwt jwt;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			log.debug("SecurityContextHolder에 이미 인증정보가 존재합니다: {}",
				SecurityContextHolder.getContext().getAuthentication());
			filterChain.doFilter(request, response);
			return;
		}

		String token = parseTokenFromHttpRequest(request);
		log.info("JwtAuthenticationFilter에서 token({}) 검증을 시작합니다...", token);
		if (token != null && jwt.validateToken(token)) {
			try {
				JwtAuthentication authentication = createJwtAuthentication(token);
				JwtAuthenticationToken authenticationToken = createJwtAuthenticationToken(request, token,
					authentication);
				SecurityContextHolder.getContext().setAuthentication(authenticationToken);
			} catch (Exception e) {
				log.warn("Jwt 처리에 실패했습니다: {}", e.getMessage());
			}
			filterChain.doFilter(request, response);
			return;
		}

		// 유효하지 않은 토큰일 경우 예외메시지를 던지는 것이 필요하다.
		log.warn("Jwt({})가 유효하지 않습니다.", jwt);
		ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INVALID_TOKEN);
		ObjectMapper objectMapper = new ObjectMapper();
		String accessDenialResponse = objectMapper.writeValueAsString(errorResponse);
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.setContentType("application/json;charset=UTF-8");
		response.getWriter().write(accessDenialResponse);
		response.getWriter().flush();
		response.getWriter().close();
	}

	private String parseTokenFromHttpRequest(HttpServletRequest request) {
		log.info("요청 메시지로부터 Authorization 헤더 값을 가져옵니다...");
		String jwtWithBearer = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (validateTokenFromRequest(jwtWithBearer)) {
			log.warn("Authorization에 값이 존재하지 않습니다.");

			return null;
		}

		log.info("Authorization({}) 값을 가져오는데 성공했습니다.", jwtWithBearer);
		String jwt = jwtWithBearer.substring(8);
		log.info("Jwt({})를 가져오는데 성공했습니다.", jwt);

		return jwt;
	}

	private boolean validateTokenFromRequest(String jwtWithBearer) {
		return StringUtils.hasText(jwtWithBearer) && jwtWithBearer.startsWith("Bearer ");
	}

	private JwtAuthenticationToken createJwtAuthenticationToken(HttpServletRequest request, String token,
		JwtAuthentication authentication) {
		Collection<? extends GrantedAuthority> authorities = jwt.getAuthorities(token);
		JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(authentication, null, authorities);
		authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

		return authenticationToken;
	}

	private JwtAuthentication createJwtAuthentication(String token) {
		String jwtSubject = jwt.getSubject(token);

		return new JwtAuthentication(jwtSubject, token);
	}
}
