package com.palpal.dealightbe.domain.auth.application;

import java.util.Map;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

public record JoinRequireRes(
	String provider,
	Long providerId,
	String nickName,
	String message
) {

	public static JoinRequireRes from(OAuth2AuthenticationToken oAuth2AuthenticationToken) {
		String provider = oAuth2AuthenticationToken.getAuthorizedClientRegistrationId();
		Long providerId = Long.parseLong(oAuth2AuthenticationToken.getPrincipal().getName());
		Map<String, Object> attributes = oAuth2AuthenticationToken.getPrincipal().getAttributes();
		@SuppressWarnings("unchecked") Map<String, Object> properties = (Map<String, Object>)attributes
			.get("properties");
		String nickName = (String)properties.get("nickname");

		return new JoinRequireRes(provider, providerId, nickName, "딜라이트 서비스에 가입이 필요합니다.");
	}
}
