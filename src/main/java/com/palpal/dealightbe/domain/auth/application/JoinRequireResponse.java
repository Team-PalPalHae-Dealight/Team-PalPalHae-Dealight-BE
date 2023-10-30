package com.palpal.dealightbe.domain.auth.application;

import java.util.Map;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

public record JoinRequireResponse(
	Long providerId,
	String nickName,
	String message
) {
	public static JoinRequireResponse of(OAuth2AuthenticationToken oAuth2AuthenticationToken) {
		Long providerId = Long.parseLong(oAuth2AuthenticationToken.getPrincipal().getName());
		Map<String, Object> attributes = oAuth2AuthenticationToken.getPrincipal().getAttributes();
		@SuppressWarnings("unchecked") Map<String, Object> properties = (Map<String, Object>)attributes
			.get("properties");
		String nickName = (String)properties.get("nickname");

		return new JoinRequireResponse(providerId, nickName, "딜라이트 서비스에 가입이 필요합니다.");
	}
}
