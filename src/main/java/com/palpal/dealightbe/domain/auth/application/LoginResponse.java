package com.palpal.dealightbe.domain.auth.application;

import java.util.Map;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

public record LoginResponse(

	Long providerId,
	String accessToken,
	String refreshToken,
	String nickName
) {
	public static LoginResponse of(String accessToken, String refreshToken,
		OAuth2AuthenticationToken oAuth2AuthenticationToken) {
		Long providerId = Long.parseLong(oAuth2AuthenticationToken.getPrincipal().getName());
		Map<String, Object> attributes = oAuth2AuthenticationToken.getPrincipal().getAttributes();
		@SuppressWarnings("unchecked") Map<String, Object> properties = (Map<String, Object>)attributes
			.get("properties");
		String nickName = (String)properties.get("profile_nickname");

		return new LoginResponse(providerId, accessToken, refreshToken, nickName);
	}
}
