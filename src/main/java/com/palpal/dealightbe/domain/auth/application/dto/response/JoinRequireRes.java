package com.palpal.dealightbe.domain.auth.application.dto.response;

import java.util.Map;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

public record JoinRequireRes(
	String provider,
	Long providerId,
	String nickName
) {
}
