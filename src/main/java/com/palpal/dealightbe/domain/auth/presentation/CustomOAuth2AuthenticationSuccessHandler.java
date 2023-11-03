package com.palpal.dealightbe.domain.auth.presentation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palpal.dealightbe.domain.auth.application.AuthService;
import com.palpal.dealightbe.domain.auth.application.dto.response.JoinRequireRes;
import com.palpal.dealightbe.domain.auth.application.dto.response.LoginRes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomOAuth2AuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	private final AuthService authService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {
		log.info("사용자({})가 소셜 로그인에 성공했습니다.", authentication.toString());
		if (authentication instanceof OAuth2AuthenticationToken oAuth2AuthenticationToken) {
			LoginRes oAuth2Response = authService.login(oAuth2AuthenticationToken);
			if (oAuth2Response == null) {
				// 사용자 정보 조회에 실패하면 회원가입 페이지로 리다이렉트 하라는 메시지를 전달한다.
				JoinRequireRes joinRequireResponse = JoinRequireRes.from(oAuth2AuthenticationToken);
				writeRequireRoleResponseToHttpMessage(response, joinRequireResponse);
				return;
			}
			// 사용자 정보 조회가 되면 새로 발급된 토큰을 반환한다.
			writeLoginSuccessResponseToHttpMessage(response, oAuth2Response);
		} else {
			super.onAuthenticationSuccess(request, response, authentication);
		}
	}

	private void writeLoginSuccessResponseToHttpMessage(HttpServletResponse response,
		LoginRes oAuth2Response) throws IOException {
		String responseValue = new ObjectMapper().writeValueAsString(oAuth2Response);
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json;charset=UTF-8");
		response.setContentLength(responseValue.getBytes(StandardCharsets.UTF_8).length);
		response.getWriter().write(responseValue);
	}

	private void writeRequireRoleResponseToHttpMessage(HttpServletResponse response,
		JoinRequireRes joinRequireResponse) throws IOException {
		String responseValue = new ObjectMapper().writeValueAsString(joinRequireResponse);
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.setContentType("application/json;charset=UTF-8");
		response.setContentLength(responseValue.getBytes(StandardCharsets.UTF_8).length);
		response.getWriter().write(responseValue);
	}
}
