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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.palpal.dealightbe.domain.auth.domain.Jwt;
import com.palpal.dealightbe.domain.auth.domain.JwtAuthentication;
import com.palpal.dealightbe.domain.auth.domain.JwtAuthenticationToken;
import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.ErrorResponse;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final Jwt jwt;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		if (SecurityContextHolder.getContext().getAuthentication() == null) {
			String token = parseTokenFromHttpRequest(request);
			log.debug("JwtAuthenticationFilter에서 token({}) 검증을 시작합니다...", token);
			if (token != null) {
				try {
					jwt.validateToken(token);
				} catch (ExpiredJwtException e) {
					log.error("JWT({})가 만료되었습니다. 만료일: {}", token, e.getClaims().getExpiration());
					// 토큰이 만료된 경우 401 Unauthorized를 보낸다.
					writeErrorResponse(response, ErrorCode.EXPIRED_TOKEN, HttpServletResponse.SC_UNAUTHORIZED);
					return;
				} catch (RuntimeException e) {
					log.error("JWT({})의 유효성(형식, 서명 등)이 올바르지 않습니다.", token);
					// 토큰이 올바르지 않은 경우 401 Unauthorized를 보낸다.
					writeErrorResponse(response, ErrorCode.INVALID_TOKEN_FORMAT, HttpServletResponse.SC_UNAUTHORIZED);
					return;
				}

				try {
					JwtAuthentication authentication = createJwtAuthentication(token);
					JwtAuthenticationToken authenticationToken = createJwtAuthenticationToken(request, token,
						authentication);
					SecurityContextHolder.getContext().setAuthentication(authenticationToken);
				} catch (RuntimeException e) {
					log.error("JWT({})로부터 인증정보를 만드는데 실패했습니다: {}", token, e.getMessage());
					// 토큰이 정상적으로 검증되었는데, 인증객체를 만드는데 실패했다면 서버 오류로 생각
					writeErrorResponse(response, ErrorCode.UNABLE_TO_CREATE_AUTHENTICATION,
						HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					return;
				}
			}
		}

		// 인증 정보가 이미 있는 경우, 토큰이 null인 경우, 인증에 성공한 경우
		log.debug("인증 과정을 마쳤으므로 다음 필터로 진행합니다...");
		filterChain.doFilter(request, response);
	}

	private String parseTokenFromHttpRequest(HttpServletRequest request) {
		log.debug("요청 메시지로부터 Authorization 헤더 값을 가져옵니다...");
		String jwtWithBearer = request.getHeader(HttpHeaders.AUTHORIZATION);
		log.debug("jwtWithBearer: {}", jwtWithBearer);
		boolean isValidToken = validateTokenFromRequest(jwtWithBearer);
		log.debug("isValidToken: {}", isValidToken);
		if (!isValidToken) {
			log.debug("Authorization에 값이 존재하지 않습니다.");

			return null;
		}

		log.debug("Authorization({}) 값을 가져오는데 성공했습니다.", jwtWithBearer);
		String jwt = jwtWithBearer.substring(7);
		log.debug("Jwt({})를 가져오는데 성공했습니다.", jwt);

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

	private void writeErrorResponse(HttpServletResponse response, ErrorCode errorCode, int statusCode) throws
		IOException {
		String errorResponseJsonFormat = getErrorResponseJsonFormat(errorCode);
		writeToHttpServletResponse(response, statusCode, errorResponseJsonFormat);
	}

	private String getErrorResponseJsonFormat(ErrorCode errorCode) throws JsonProcessingException {
		ErrorResponse errorResponse = ErrorResponse.of(errorCode);
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.writeValueAsString(errorResponse);
	}

	private void writeToHttpServletResponse(HttpServletResponse response, int statusCode, String errorMessage) throws
		IOException {
		response.setStatus(statusCode);
		response.setContentType("application/json;charset=UTF-8");
		response.getWriter().write(errorMessage);
		response.getWriter().flush();
		response.getWriter().close();
	}
}
